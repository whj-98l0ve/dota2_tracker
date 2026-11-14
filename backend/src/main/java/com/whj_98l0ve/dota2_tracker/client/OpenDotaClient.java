package com.whj_98l0ve.dota2_tracker.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class OpenDotaClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${opendota.base-url:https://api.opendota.com/api}")
    private String baseUrl;

    @Value("${opendota.api-key:}")
    private String apiKey;

    // 1. 每 2 秒 1 次，启动有 3 秒平滑过渡，杜绝冷启动突发脉冲
    private final RateLimiter rateLimiter = RateLimiter.create(0.5, Duration.ofSeconds(3));

    // 2. 熔断器配置与状态
    // 连续出现 3 次 429 或 5xx (500/502/503/521 等) 错误时触发全局熔断
    private static final int CIRCUIT_BREAKER_THRESHOLD = 3;
    // 熔断持续时间：10 分钟 (600,000 ms)
    private static final long CIRCUIT_BREAKER_DURATION_MS = 10 * 60 * 1000L;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private enum CircuitState {
        CLOSED,     // 正常状态
        OPEN,       // 熔断开启（拒绝所有请求，等待 10 分钟）
        HALF_OPEN   // 半开探测状态（10 分钟期满后放行一次探测请求）
    }

    private volatile CircuitState circuitState = CircuitState.CLOSED;
    private volatile int consecutiveFailures = 0;
    private volatile long circuitOpenUntil = 0L;

    public OpenDotaClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);   // 3 秒连接超时
        factory.setReadTimeout(15000);      // 15 秒读取，防止大 JSON 还没传完就被切断
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 判断当前是否处于熔断拦截期（若已满 10 分钟需要探测，则返回 false 允许触发探测请求）
     */
    public boolean isCircuitBreakerOpen() {
        return circuitState == CircuitState.OPEN && System.currentTimeMillis() < circuitOpenUntil;
    }

    /**
     * 获取当前熔断剩余秒数（未熔断返回 0）
     */
    public long getCircuitBreakerRemainingSeconds() {
        if (!isCircuitBreakerOpen()) {
            return 0L;
        }
        return Math.max(0L, (circuitOpenUntil - System.currentTimeMillis()) / 1000L);
    }

    private String buildUrl(String path) {
        String url = baseUrl + (path.startsWith("/") ? path : "/" + path);
        if (apiKey != null && !apiKey.isBlank()) {
            url += (url.contains("?") ? "&" : "?") + "api_key=" + apiKey;
        }
        return url;
    }

    public JsonNode get(String path) {
        String fullUrl = buildUrl(path);
        return executeWithRateLimit(path, () -> {
            // 先接收 String 文本，避免 RestTemplate 内部自动反序列化抛出无法挽回的提取异常
            String rawJson = restTemplate.getForObject(fullUrl, String.class);
            if (rawJson == null || rawJson.trim().isEmpty()) {
                log.warn("OpenDota 返回空响应体: {}", path);
                return null;
            }
            try {
                JsonNode node = objectMapper.readTree(rawJson);
                // 若 OpenDota 返回类似 {"error":"Internal Server Error"} 的 200 包裹错误对象，同样计入服务端 5xx 错误
                if (node != null && node.isObject() && node.has("error")) {
                    String errText = node.path("error").asText();
                    log.warn("OpenDota 返回错误响应: {} -> {}", path, errText);
                    recordServerOrRateLimitFailure(path, "ErrorBody(" + errText + ")");
                    return null;
                }
                // 请求正常解析成功，若此前有失败计数或处于半开探测状态，执行恢复重置
                onRequestSuccess(path);
                return node;
            } catch (Exception parseEx) {
                log.warn("JSON 语法解析失败 (可能数据被截断): {} -> {}", path, parseEx.getMessage());
                return null;
            }
        });
    }

    public JsonNode post(String path, Object body) {
        String fullUrl = buildUrl(path);
        return executeWithRateLimit(path, () -> {
            ResponseEntity<String> res = restTemplate.postForEntity(fullUrl, body, String.class);
            String rawJson = res.getBody();
            if (rawJson == null || rawJson.trim().isEmpty()) {
                return null;
            }
            try {
                JsonNode node = objectMapper.readTree(rawJson);
                if (node != null && node.isObject() && node.has("error")) {
                    String errText = node.path("error").asText();
                    log.warn("OpenDota POST 返回错误响应: {} -> {}", path, errText);
                    recordServerOrRateLimitFailure(path, "ErrorBody(" + errText + ")");
                    return null;
                }
                onRequestSuccess(path);
                return node;
            } catch (Exception parseEx) {
                log.warn("POST 响应 JSON 解析失败: {} -> {}", path, parseEx.getMessage());
                return null;
            }
        });
    }

    private synchronized JsonNode executeWithRateLimit(String path, ApiSupplier supplier) {
        long now = System.currentTimeMillis();

        // 第一道防线：熔断器状态检查
        if (circuitState == CircuitState.OPEN) {
            if (now < circuitOpenUntil) {
                long waitSeconds = (circuitOpenUntil - now) / 1000;
                log.warn("⛔ [熔断器开启中] 剩余 {} 秒恢复探测，跳过远程请求 [{}]...", waitSeconds, path);
                return null;
            } else {
                // 已满 10 分钟，进入半开状态，放行当前请求作为恢复探测
                circuitState = CircuitState.HALF_OPEN;
                log.info("🔍 [熔断器半开探测] 10 分钟熔断冷却期已满，正在对 [{}] 发起恢复探测请求...", path);
            }
        }

        // 第二道防线：单通道发牌器（严格 2 秒 1 次）
        rateLimiter.acquire();

        try {
            return supplier.doRequest();
        } catch (RestClientResponseException e) {
            int statusCode = e.getStatusCode().value();
            // 捕获 429 限流、521 (Cloudflare 源站宕机) 或 500/502/503/504 等所有 5xx 服务端错误
            if (statusCode == 429 || statusCode == 521 || statusCode >= 500) {
                log.warn("⚠️ 请求 OpenDota 触发限流或服务端错误 [{}] (HTTP {}): {}", path, statusCode, e.getStatusText());
                recordServerOrRateLimitFailure(path, "HTTP " + statusCode);
            } else {
                log.warn("请求 OpenDota 客户端异常 [{}] (HTTP {}): {}", path, statusCode, e.getMessage());
            }
            return null;
        } catch (ResourceAccessException e) {
            // 网络超时：打印含查询参数的完整 path；若发生在半开探测期间，说明网络/服务仍未恢复，重新延长熔断
            log.warn("请求 OpenDota 网络超时 [{}]: {}", path, e.getMessage());
            if (circuitState == CircuitState.HALF_OPEN) {
                tripCircuitBreaker(path, "半开探测请求网络超时");
            }
            return null;
        } catch (Exception e) {
            log.warn("请求 OpenDota 异常 [{}]: {}", path, e.getMessage());
            if (circuitState == CircuitState.HALF_OPEN) {
                tripCircuitBreaker(path, "半开探测请求异常: " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * 请求成功时重置连续失败计数；若处于半开探测状态则关闭熔断器恢复正常
     */
    private void onRequestSuccess(String path) {
        if (circuitState == CircuitState.HALF_OPEN) {
            circuitState = CircuitState.CLOSED;
            consecutiveFailures = 0;
            circuitOpenUntil = 0L;
            log.info("✅ [熔断器恢复] 探测请求 [{}] 成功！OpenDota 服务已恢复正常，熔断器已关闭。", path);
        } else if (consecutiveFailures > 0) {
            consecutiveFailures = 0;
        }
    }

    /**
     * 记录 429 / 521 / 5xx 错误次数，达到阈值或处于半开探测时立即触发 10 分钟熔断
     */
    private void recordServerOrRateLimitFailure(String path, String reason) {
        if (circuitState == CircuitState.HALF_OPEN) {
            // 半开探测失败，立即重新进入 10 分钟熔断
            tripCircuitBreaker(path, "恢复探测失败 (" + reason + ")");
            return;
        }

        consecutiveFailures++;
        if (consecutiveFailures >= CIRCUIT_BREAKER_THRESHOLD) {
            tripCircuitBreaker(path, "连续 " + consecutiveFailures + " 次出现 429/5xx 错误 (最近原因: " + reason + ")");
        } else {
            log.warn("⚠️ [熔断计数 {}/{}] 接口 [{}] 出现异常 ({})，若连续达到 {} 次将触发 10 分钟熔断",
                    consecutiveFailures, CIRCUIT_BREAKER_THRESHOLD, path, reason, CIRCUIT_BREAKER_THRESHOLD);
        }
    }

    /**
     * 触发/延长 10 分钟熔断
     */
    private void tripCircuitBreaker(String path, String reason) {
        circuitState = CircuitState.OPEN;
        circuitOpenUntil = System.currentTimeMillis() + CIRCUIT_BREAKER_DURATION_MS;
        String resumeTimeStr = LocalDateTime.ofInstant(Instant.ofEpochMilli(circuitOpenUntil), ZoneId.systemDefault())
                .format(TIME_FMT);
        log.error("🚨 [熔断器已触发] 接口 [{}] {}！已暂停所有 OpenDota 请求，将在 10 分钟后 ({}) 自动恢复探测...",
                path, reason, resumeTimeStr);
    }

    @FunctionalInterface
    private interface ApiSupplier {
        JsonNode doRequest() throws Exception;
    }
}
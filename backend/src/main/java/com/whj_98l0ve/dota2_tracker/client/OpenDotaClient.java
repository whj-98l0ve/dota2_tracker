package com.whj_98l0ve.dota2_tracker.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

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

    // 2. 全局冷却时间戳：一旦撞到 429 或严重网络超时，所有线程在此期间跳过远程请求
    private volatile long coolDownUntil = 0L;

    public OpenDotaClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);   // 3 秒连接超时
        factory.setReadTimeout(15000);      // 15 秒读取，防止大 JSON 还没传完就被切断
        this.restTemplate = new RestTemplate(factory);
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
        return executeWithRateLimit(() -> {
            // 核心修改：先接收 String 文本，避免 RestTemplate 内部自动反序列化抛出无法挽回的提取异常
            String rawJson = restTemplate.getForObject(fullUrl, String.class);
            if (rawJson == null || rawJson.trim().isEmpty()) {
                log.warn("OpenDota 返回空响应体: {}", path);
                return null;
            }
            try {
                return objectMapper.readTree(rawJson);
            } catch (Exception parseEx) {
                log.warn("JSON 语法解析失败 (可能数据被截断): {} -> {}", path, parseEx.getMessage());
                return null;
            }
        });
    }

    public JsonNode post(String path, Object body) {
        String fullUrl = buildUrl(path);
        return executeWithRateLimit(() -> {
            ResponseEntity<String> res = restTemplate.postForEntity(fullUrl, body, String.class);
            String rawJson = res.getBody();
            if (rawJson == null || rawJson.trim().isEmpty()) {
                return null;
            }
            try {
                return objectMapper.readTree(rawJson);
            } catch (Exception parseEx) {
                log.warn("POST 响应 JSON 解析失败: {} -> {}", path, parseEx.getMessage());
                return null;
            }
        });
    }

    private synchronized JsonNode executeWithRateLimit(ApiSupplier supplier) {
        long now = System.currentTimeMillis();
        // 第一道防线：检查是否处于熔断/限流冷却期中
        if (now < coolDownUntil) {
            long waitTime = coolDownUntil - now;
            log.warn("⚠️ 处于网络故障/限流冷却期中（剩余 {} 秒），跳过远程请求直接返回空...", waitTime / 1000);
            return null;
        }

        // 第二道防线：单通道发牌器（严格 2 秒 1 次）
        rateLimiter.acquire();

        try {
            return supplier.doRequest();
        } catch (HttpClientErrorException.TooManyRequests e) {
            // 撞到 429，开启全应用 60 秒硬性冷却
            log.warn("⚠️ 命中 OpenDota 分钟限流！激活全应用 60 秒硬性冷却熔断...");
            coolDownUntil = System.currentTimeMillis() + 60000L;
            return null;
        } catch (ResourceAccessException e) {
            // 网络超时：激活 15 秒熔断冷却，避免连续请求同一失效通道卡死线程
            log.warn("请求 OpenDota 网络超时，开启 15 秒降级保护: {}", e.getMessage());
            coolDownUntil = System.currentTimeMillis() + 15000L;
            return null;
        } catch (Exception e) {
            log.warn("请求 OpenDota 异常: {}", e.getMessage());
            return null;
        }
    }

    @FunctionalInterface
    private interface ApiSupplier {
        JsonNode doRequest() throws Exception;
    }
}
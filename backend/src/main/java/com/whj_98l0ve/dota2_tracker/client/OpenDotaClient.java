package com.whj_98l0ve.dota2_tracker.client;

import com.fasterxml.jackson.databind.JsonNode;
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

    @Value("${opendota.base-url:https://api.opendota.com/api}")
    private String baseUrl;

    @Value("${opendota.api-key:}")
    private String apiKey;

    // 1. 每 2 秒 1 次，启动有 3 秒平滑过渡，杜绝冷启动突发脉冲
    private final RateLimiter rateLimiter = RateLimiter.create(0.5, Duration.ofSeconds(3));

    // 2. 全局冷却时间戳：一旦撞到 429，所有线程强制等待冷却，谁也别想抢跑
    private volatile long coolDownUntil = 0L;

    public OpenDotaClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);   // 5 秒连接超时
        factory.setReadTimeout(30000);     // 30 秒读取超时（大录像数据）
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
        return executeWithRateLimit(() -> restTemplate.getForObject(fullUrl, JsonNode.class));
    }

    public JsonNode post(String path, Object body) {
        String fullUrl = buildUrl(path);
        return executeWithRateLimit(() -> {
            ResponseEntity<JsonNode> res = restTemplate.postForEntity(fullUrl, body, JsonNode.class);
            return res.getBody();
        });
    }

    private synchronized JsonNode executeWithRateLimit(ApiSupplier supplier) {
        // 第一道防线：检查是否处于 429 惩罚冷却期
        long now = System.currentTimeMillis();
        if (now < coolDownUntil) {
            long waitTime = coolDownUntil - now;
            log.info("处于全局限流冷却期中，全应用挂起等待 {} 秒...", waitTime / 1000);
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 第二道防线：单通道发牌器（严格 2 秒 1 次）
        rateLimiter.acquire();

        try {
            return supplier.doRequest();
        } catch (HttpClientErrorException.TooManyRequests e) {
            // 撞到 429，开启全应用 60 秒硬性冷却（正好等待 OpenDota 官方 1 分钟窗口完全归零）
            log.warn("⚠️ 命中 OpenDota 分钟限流！激活全应用 60 秒硬性冷却熔断...");
            coolDownUntil = System.currentTimeMillis() + 60000L;
            try {
                Thread.sleep(60000L);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            // 冷却结束后重试一次
            rateLimiter.acquire();
            try {
                return supplier.doRequest();
            } catch (Exception ex) {
                log.error("60秒冷却后重试依然失败: {}", ex.getMessage());
                return null;
            }
        } catch (ResourceAccessException e) {
            log.warn("请求 OpenDota 网络超时: {}", e.getMessage());
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
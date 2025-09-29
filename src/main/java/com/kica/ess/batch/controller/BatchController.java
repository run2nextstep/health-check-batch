package com.kica.ess.batch.controller;

import com.kica.ess.batch.config.BatchProperties;
import com.kica.ess.batch.dto.HealthCheckResult;
import com.kica.ess.batch.job.HealthCheckBatch;
import com.kica.ess.batch.service.HealthCheckService;
import com.kica.ess.batch.service.TelegramService;
import com.kica.ess.batch.service.TargetServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/batch")
public class BatchController {

  private static final Logger logger = LoggerFactory.getLogger(BatchController.class);

  @Autowired
  private HealthCheckBatch healthCheckBatch;

  @Autowired
  private HealthCheckService healthCheckService;

  @Autowired
  private TelegramService telegramService;

  @Autowired
  private TargetServerService targetServerService;

  @Autowired(required = false)
  private BatchProperties batchProperties;

  @GetMapping("/health")
  public ResponseEntity<Map<String, Object>> health() {
    Map<String, Object> response = new HashMap<>();
    response.put("status", "UP");
    response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    response.put("application", "Health Check Batch");
    response.put("version", "1.0.0");
    response.put("activeServers", targetServerService.getActiveServerCount());
    response.put("totalServers", targetServerService.getTotalServerCount());

    return ResponseEntity.ok(response);
  }

  // Support both GET and POST for trigger endpoint
  @RequestMapping(value = "/trigger", method = {RequestMethod.GET, RequestMethod.POST})
  public ResponseEntity<Map<String, Object>> triggerManualHealthCheck() {
    logger.info("Manual health check trigger requested");

    try {
      healthCheckBatch.triggerManualHealthCheck();

      Map<String, Object> response = new HashMap<>();
      response.put("status", "success");
      response.put("message", "Health check batch triggered successfully");
      response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
      response.put("serversChecked", targetServerService.getActiveServerCount());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      logger.error("Failed to trigger manual health check", e);

      Map<String, Object> response = new HashMap<>();
      response.put("status", "error");
      response.put("message", "Failed to trigger health check: " + e.getMessage());
      response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
      response.put("error", e.getClass().getSimpleName());

      return ResponseEntity.internalServerError().body(response);
    }
  }

  @GetMapping("/check-now")
  public ResponseEntity<Map<String, Object>> checkNow() {
    logger.info("Immediate health check requested");

    try {
      List<HealthCheckResult> results = healthCheckService.performHealthCheck();

      Map<String, Object> response = new HashMap<>();
      response.put("status", "success");
      response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
      response.put("results", results);
      response.put("summary", buildSummary(results));

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      logger.error("Failed to perform immediate health check", e);

      Map<String, Object> response = new HashMap<>();
      response.put("status", "error");
      response.put("message", "Failed to perform health check: " + e.getMessage());
      response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
      response.put("error", e.getClass().getSimpleName());

      return ResponseEntity.internalServerError().body(response);
    }
  }

  // Support both GET and POST for telegram test
  @RequestMapping(value = "/telegram/test", method = {RequestMethod.GET, RequestMethod.POST})
  public ResponseEntity<Map<String, Object>> testTelegram() {
    logger.info("Telegram test message requested");

    try {
      telegramService.sendTestMessage();

      Map<String, Object> response = new HashMap<>();
      response.put("status", "success");
      response.put("message", "Test message sent to Telegram");
      response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      logger.error("Failed to send test Telegram message", e);

      Map<String, Object> response = new HashMap<>();
      response.put("status", "error");
      response.put("message", "Failed to send test message: " + e.getMessage());
      response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
      response.put("error", e.getClass().getSimpleName());

      return ResponseEntity.internalServerError().body(response);
    }
  }

  @GetMapping("/config")
  public ResponseEntity<Map<String, Object>> getConfig() {
    Map<String, Object> response = new HashMap<>();
    response.put("activeServers", targetServerService.getActiveServerCount());
    response.put("totalServers", targetServerService.getTotalServerCount());

    if (batchProperties != null) {
      if (batchProperties.getSchedule() != null) {
        response.put("schedule", batchProperties.getSchedule().getCron());
      }
      if (batchProperties.getTimeout() != null) {
        response.put("timeoutThreshold", batchProperties.getTimeout().getThreshold());
      }
      if (batchProperties.getTelegram() != null) {
        response.put("telegramEnabled", batchProperties.getTelegram().isEnabled());
      }
    }

    response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

    return ResponseEntity.ok(response);
  }

  @GetMapping("/stats")
  public ResponseEntity<Map<String, Object>> getStats() {
    try {
      Map<String, Object> response = new HashMap<>();

      // Recent logs stats
      response.put("recentFailures", healthCheckService.getRecentFailures(24).size());
      response.put("recentLogs", healthCheckService.getRecentLogs(24).size());

      long threshold = 10000L; // default threshold
      if (batchProperties != null && batchProperties.getTimeout() != null) {
        threshold = batchProperties.getTimeout().getThreshold();
      }
      response.put("recentSlowResponses", healthCheckService.getSlowResponses(24, threshold).size());

      response.put("activeServers", targetServerService.getActiveServerCount());
      response.put("totalServers", targetServerService.getTotalServerCount());
      response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      logger.error("Failed to get stats", e);

      Map<String, Object> response = new HashMap<>();
      response.put("status", "error");
      response.put("message", "Failed to get stats: " + e.getMessage());
      response.put("error", e.getClass().getSimpleName());

      return ResponseEntity.internalServerError().body(response);
    }
  }

  // API endpoints info
  @GetMapping("/")
  public ResponseEntity<Map<String, Object>> apiInfo() {
    Map<String, Object> response = new HashMap<>();
    response.put("application", "Health Check Batch API");
    response.put("version", "1.0.0");
    response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

    Map<String, String> endpoints = new HashMap<>();
    endpoints.put("GET /api/batch/health", "Application health status");
    endpoints.put("GET|POST /api/batch/trigger", "Trigger manual health check");
    endpoints.put("GET /api/batch/check-now", "Immediate health check with results");
    endpoints.put("GET|POST /api/batch/telegram/test", "Test Telegram notifications");
    endpoints.put("GET /api/batch/config", "View application configuration");
    endpoints.put("GET /api/batch/stats", "View statistics");

    response.put("endpoints", endpoints);
    response.put("webConsole", "http://localhost:8080/console/");
    response.put("h2Console", "http://localhost:8080/h2-console");

    return ResponseEntity.ok(response);
  }

  // Test endpoint for debugging
  @GetMapping("/test")
  public ResponseEntity<Map<String, Object>> test() {
    Map<String, Object> response = new HashMap<>();
    response.put("status", "API is working");
    response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    response.put("activeProfile", System.getProperty("spring.profiles.active", "default"));

    // Test service availability
    try {
      response.put("targetServerService", targetServerService != null ? "Available" : "Not available");
      response.put("healthCheckService", healthCheckService != null ? "Available" : "Not available");
      response.put("telegramService", telegramService != null ? "Available" : "Not available");
      response.put("batchProperties", batchProperties != null ? "Available" : "Not available");
    } catch (Exception e) {
      response.put("serviceError", e.getMessage());
    }

    return ResponseEntity.ok(response);
  }

  private Map<String, Object> buildSummary(List<HealthCheckResult> results) {
    long successCount = results.stream().filter(HealthCheckResult::isSuccess).count();
    long failureCount = results.size() - successCount;

    long threshold; // default threshold
    if (batchProperties != null && batchProperties.getTimeout() != null) {
      threshold = batchProperties.getTimeout().getThreshold();
    } else {
      threshold = 10000L;
    }

    long slowCount = results.stream()
        .filter(r -> r.isSlowResponse(threshold))
        .count();

    double averageResponseTime = results.stream()
        .filter(HealthCheckResult::isSuccess)
        .mapToLong(HealthCheckResult::getElapsedTime)
        .average()
        .orElse(0.0);

    Map<String, Object> summary = new HashMap<>();
    summary.put("totalServers", results.size());
    summary.put("successCount", successCount);
    summary.put("failureCount", failureCount);
    summary.put("slowResponseCount", slowCount);
    summary.put("averageResponseTime", Math.round(averageResponseTime));
    summary.put("timeoutThreshold", threshold);

    return summary;
  }
}
package com.kica.ess.batch.job;

import com.kica.ess.batch.config.BatchProperties;
import com.kica.ess.batch.dto.HealthCheckResult;
import com.kica.ess.batch.service.HealthCheckService;
import com.kica.ess.batch.service.TelegramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HealthCheckBatch {

  private static final Logger logger = LoggerFactory.getLogger(HealthCheckBatch.class);

  @Autowired
  private HealthCheckService healthCheckService;

  @Autowired
  private TelegramService telegramService;

  @Autowired
  private BatchProperties batchProperties;

  @Scheduled(cron = "${batch.health-check.schedule.cron:0 */5 * * * *}")
  public void executeHealthCheck() {
    logger.info("=== Health Check Batch Started at {} ===",
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

    long batchStartTime = System.currentTimeMillis();

    try {
      // Perform health checks (now uses database)
      List<HealthCheckResult> results = healthCheckService.performHealthCheck();

      // Process results
      processResults(results);

      long batchEndTime = System.currentTimeMillis();
      long totalElapsedTime = batchEndTime - batchStartTime;

      logger.info("=== Health Check Batch Completed in {}ms ===", totalElapsedTime);

      // Check if batch execution itself exceeded threshold
      long threshold = batchProperties.getTimeout() != null ?
          batchProperties.getTimeout().getThreshold() : 10000L;

      if (totalElapsedTime > threshold) {
        logger.warn("Batch execution time ({}ms) exceeded threshold ({}ms)",
            totalElapsedTime, threshold);

        String message = String.format(
            "⚠️ *Batch Execution Alert*\n\n" +
                "Batch execution time: %dms\n" +
                "Threshold: %dms\n" +
                "Time: %s",
            totalElapsedTime,
            threshold,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        telegramService.sendMessage(message);
      }

    } catch (Exception e) {
      logger.error("Health check batch execution failed", e);

      String errorMessage = String.format(
          "🚨 *Batch Execution Error*\n\n" +
              "Error: %s\n" +
              "Time: %s",
          e.getMessage(),
          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
      );

      telegramService.sendMessage(errorMessage);
    }
  }

  private void processResults(List<HealthCheckResult> results) {
    // Log results
    logResults(results);

    // Check for failures
    List<HealthCheckResult> failedResults = results.stream()
        .filter(result -> !result.isSuccess())
        .collect(Collectors.toList());

    if (!failedResults.isEmpty()) {
      logger.warn("Found {} failed health checks", failedResults.size());
      telegramService.sendFailureAlert(failedResults);
    }

    // Check for slow responses
    long threshold = batchProperties.getTimeout() != null ?
        batchProperties.getTimeout().getThreshold() : 500L;

    List<HealthCheckResult> slowResults = results.stream()
        .filter(result -> result.isSuccess() &&
            result.isSlowResponse(threshold))
        .collect(Collectors.toList());

    if (!slowResults.isEmpty()) {
      logger.warn("Found {} slow responses", slowResults.size());
      telegramService.sendSlowResponseAlert(slowResults);
    }

    // Success summary
    long successCount = results.stream().filter(HealthCheckResult::isSuccess).count();
    logger.info("Health check summary: {}/{} servers responded successfully",
        successCount, results.size());
  }

  private void logResults(List<HealthCheckResult> results) {
    logger.info("Health check results:");
    for (HealthCheckResult result : results) {
      if (result.isSuccess()) {
        logger.info("✅ {} - {}ms - {}",
            result.getServerName(),
            result.getElapsedTime(),
            result.getUrl());

        // Log response details if available
        if (result.getResponseJson() != null) {
          logger.debug("Response from {}: {}",
              result.getServerName(),
              result.getResponseJson().toString());
        }
      } else {
        logger.error("❌ {} - {}ms - {} - Error: {}",
            result.getServerName(),
            result.getElapsedTime(),
            result.getUrl(),
            result.getErrorMessage());
      }
    }
  }

  // Manual trigger for testing
  public void triggerManualHealthCheck() {
    logger.info("Manual health check triggered");
    executeHealthCheck();
  }
}
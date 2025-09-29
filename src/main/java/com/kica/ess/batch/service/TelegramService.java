package com.kica.ess.batch.service;

import com.kica.ess.batch.config.BatchProperties;
import com.kica.ess.batch.dto.HealthCheckResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TelegramService {

  private static final Logger logger = LoggerFactory.getLogger(TelegramService.class);
  private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";

  @Autowired(required = false)
  private BatchProperties batchProperties;

  @Autowired
  private WebClient webClient;

  public void sendSlowResponseAlert(List<HealthCheckResult> slowResults) {
    if (!isTelegramEnabled()) {
      logger.debug("Telegram notifications are disabled");
      return;
    }

    if (slowResults.isEmpty()) {
      logger.debug("No slow responses to report");
      return;
    }

    String message = buildSlowResponseMessage(slowResults);
    sendMessage(message);
  }

  public void sendFailureAlert(List<HealthCheckResult> failedResults) {
    if (!isTelegramEnabled()) {
      logger.debug("Telegram notifications are disabled");
      return;
    }

    if (failedResults.isEmpty()) {
      logger.debug("No failures to report");
      return;
    }

    String message = buildFailureMessage(failedResults);
    sendMessage(message);
  }

  public void sendTestMessage() {
    String message = "🧪 *Test Message*\n\nHealth Check Batch is running successfully!\n\n" +
        "Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    sendMessage(message);
  }

  public void sendMessage(String message) {
    if (!isTelegramEnabled()) {
      logger.warn("Telegram is not enabled, cannot send message");
      return;
    }

    try {
      String botToken = batchProperties.getTelegram().getBotToken();
      String chatId = batchProperties.getTelegram().getChatId();

      if (botToken == null || chatId == null || botToken.trim().isEmpty() || chatId.trim().isEmpty()) {
        logger.error("Telegram bot token or chat ID is not configured properly");
        return;
      }

      String url = TELEGRAM_API_URL + botToken + "/sendMessage";

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("chat_id", chatId);
      requestBody.put("text", message);
      requestBody.put("parse_mode", "Markdown");

      logger.debug("Sending Telegram message to URL: {}", url);
      logger.debug("Message content: {}", message);

      String response = webClient.post()
          .uri(url)
          .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .body(BodyInserters.fromValue(requestBody))
          .retrieve()
          .bodyToMono(String.class)
          .block();

      logger.info("Telegram message sent successfully. Response: {}", response);

    } catch (Exception e) {
      logger.error("Failed to send Telegram message: {}", e.getMessage(), e);
    }
  }

  public void sendBatchExecutionAlert(String alertType, long executionTime, long threshold) {
    if (!isTelegramEnabled()) {
      return;
    }

    String message = String.format(
        "⚠️ *%s*\n\n" +
            "Execution time: %dms\n" +
            "Threshold: %dms\n" +
            "Time: %s",
        alertType,
        executionTime,
        threshold,
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    );

    sendMessage(message);
  }

  public void sendErrorAlert(String errorMessage) {
    if (!isTelegramEnabled()) {
      return;
    }

    String message = String.format(
        "🚨 *Health Check Error*\n\n" +
            "Error: %s\n" +
            "Time: %s",
        errorMessage,
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    );

    sendMessage(message);
  }

  private String buildSlowResponseMessage(List<HealthCheckResult> slowResults) {
    StringBuilder sb = new StringBuilder();
    sb.append("🐌 *Slow Response Alert*\n\n");
    sb.append("⏰ Time: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");

    long threshold = getThreshold();
    sb.append("🎯 Threshold: ").append(threshold).append("ms\n\n");

    for (HealthCheckResult result : slowResults) {
      sb.append("🔸 *").append(result.getServerName()).append("*\n");
      sb.append("   URL: ").append(result.getUrl()).append("\n");
      sb.append("   Method: ").append(result.getMethod()).append("\n");
      sb.append("   Response Time: ").append(result.getElapsedTime()).append("ms\n");
      sb.append("   Status: ").append(result.isSuccess() ? "✅ Success" : "❌ Failed").append("\n\n");
    }

    return sb.toString();
  }

  private String buildFailureMessage(List<HealthCheckResult> failedResults) {
    StringBuilder sb = new StringBuilder();
    sb.append("🚨 *Health Check Failure Alert*\n\n");
    sb.append("⏰ Time: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

    for (HealthCheckResult result : failedResults) {
      sb.append("🔸 *").append(result.getServerName()).append("*\n");
      sb.append("   URL: ").append(result.getUrl()).append("\n");
      sb.append("   Method: ").append(result.getMethod()).append("\n");
      sb.append("   Error: ").append(result.getErrorMessage() != null ? result.getErrorMessage() : "Unknown error").append("\n");
      sb.append("   Response Time: ").append(result.getElapsedTime()).append("ms\n\n");
    }

    return sb.toString();
  }

  private boolean isTelegramEnabled() {
    if (batchProperties == null) {
      logger.debug("BatchProperties is not configured");
      return false;
    }

    if (batchProperties.getTelegram() == null) {
      logger.debug("Telegram configuration is not available");
      return false;
    }

    return batchProperties.getTelegram().isEnabled();
  }

  private long getThreshold() {
    if (batchProperties != null && batchProperties.getTimeout() != null) {
      return batchProperties.getTimeout().getThreshold();
    }
    return 10000L; // Default 10 seconds
  }

  public boolean isTelegramConfigured() {
    if (!isTelegramEnabled()) {
      return false;
    }

    String botToken = batchProperties.getTelegram().getBotToken();
    String chatId = batchProperties.getTelegram().getChatId();

    return botToken != null && !botToken.trim().isEmpty() &&
        chatId != null && !chatId.trim().isEmpty();
  }

  public Map<String, Object> getTelegramStatus() {
    Map<String, Object> status = new HashMap<>();
    status.put("enabled", isTelegramEnabled());
    status.put("configured", isTelegramConfigured());

    if (batchProperties != null && batchProperties.getTelegram() != null) {
      String botToken = batchProperties.getTelegram().getBotToken();
      String chatId = batchProperties.getTelegram().getChatId();

      status.put("hasToken", botToken != null && !botToken.trim().isEmpty());
      status.put("hasChatId", chatId != null && !chatId.trim().isEmpty());

      if (botToken != null && botToken.length() > 10) {
        status.put("tokenMasked", botToken.substring(0, 10) + "...");
      }

      if (chatId != null && chatId.length() > 5) {
        status.put("chatIdMasked", chatId.substring(0, 3) + "..." + chatId.substring(chatId.length() - 2));
      }
    } else {
      status.put("hasToken", false);
      status.put("hasChatId", false);
    }

    return status;
  }

  public boolean testTelegramConnection() {
    if (!isTelegramConfigured()) {
      logger.warn("Telegram is not properly configured for testing");
      return false;
    }

    try {
      String botToken = batchProperties.getTelegram().getBotToken();
      String url = TELEGRAM_API_URL + botToken + "/getMe";

      String response = webClient.get()
          .uri(url)
          .retrieve()
          .bodyToMono(String.class)
          .block();

      logger.info("Telegram API test successful: {}", response);
      return response != null && response.contains("\"ok\":true");

    } catch (Exception e) {
      logger.error("Telegram API test failed: {}", e.getMessage(), e);
      return false;
    }
  }
}
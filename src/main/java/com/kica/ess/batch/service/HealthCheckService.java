package com.kica.ess.batch.service;

import com.kica.ess.batch.config.BatchProperties;
import com.kica.ess.batch.dto.HealthCheckResult;
import com.kica.ess.batch.entity.ExecutionLog;
import com.kica.ess.batch.entity.TargetServer;
import com.kica.ess.batch.repository.ExecutionLogRepository;
import com.kica.ess.batch.repository.TargetServerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class HealthCheckService {

  private static final Logger logger = LoggerFactory.getLogger(HealthCheckService.class);

  @Autowired
  private TargetServerRepository targetServerRepository;

  @Autowired
  private ExecutionLogRepository executionLogRepository;

  @Autowired
  private BatchProperties batchProperties;

  @Autowired
  private WebClient webClient;

  @Autowired
  private ObjectMapper objectMapper;

  @Value("${spring.profiles.active:dev}")
  private String activeProfile;

  public List<HealthCheckResult> performHealthCheck() {
    String batchExecutionId = UUID.randomUUID().toString();
    List<TargetServer> servers = targetServerRepository.findActiveServersByEnvironment(activeProfile);

    logger.info("Starting health check for {} servers (batch: {})", servers.size(), batchExecutionId);

    List<HealthCheckResult> results = new ArrayList<>();

    for (TargetServer server : servers) {
      HealthCheckResult result = checkSingleServer(server, batchExecutionId);
      results.add(result);

      // Save execution log to database
      saveExecutionLog(server, result, batchExecutionId);
    }

    logger.info("Health check completed. Results: {} (batch: {})", results.size(), batchExecutionId);
    return results;
  }

  private HealthCheckResult checkSingleServer(TargetServer server, String batchExecutionId) {
    logger.debug("Checking server: {} - {}", server.getName(), server.getUrl());

    long startTime = System.currentTimeMillis();
    HealthCheckResult result = new HealthCheckResult();
    result.setServerName(server.getName());
    result.setUrl(server.getUrl());
    result.setMethod(server.getMethod());
    result.setStartTime(startTime);

    try {
      WebClient.RequestHeadersSpec<?> requestSpec;

      if ("POST".equalsIgnoreCase(server.getMethod())) {
        requestSpec = webClient.post()
            .uri(server.getUrl())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(server.getRequestBody() != null ?
                BodyInserters.fromValue(server.getRequestBody()) :
                BodyInserters.empty());
      } else {
        requestSpec = webClient.get().uri(server.getUrl());
      }

      String response = requestSpec
          .retrieve()
          .bodyToMono(String.class)
          .timeout(Duration.ofMillis(server.getTimeout()))
          .block();

      long endTime = System.currentTimeMillis();
      long elapsedTime = endTime - startTime;

      result.setEndTime(endTime);
      result.setElapsedTime(elapsedTime);
      result.setSuccess(true);
      result.setResponse(response);
      result.setStatusCode(200); // WebClient successful response

      // Parse JSON response
      if (response != null && !response.isEmpty()) {
        try {
          JsonNode jsonNode = objectMapper.readTree(response);
          result.setResponseJson(jsonNode);
          logger.debug("Server {} responded successfully in {}ms",
              server.getName(), elapsedTime);
        } catch (Exception e) {
          logger.warn("Failed to parse JSON response from {}: {}",
              server.getName(), e.getMessage());
          result.setResponseJson(objectMapper.createObjectNode()
              .put("raw_response", response));
        }
      }

    } catch (Exception e) {
      long endTime = System.currentTimeMillis();
      long elapsedTime = endTime - startTime;

      result.setEndTime(endTime);
      result.setElapsedTime(elapsedTime);
      result.setSuccess(false);
      result.setErrorMessage(e.getMessage());
      result.setStatusCode(0); // Unknown status for exceptions

      logger.error("Health check failed for server {}: {}",
          server.getName(), e.getMessage(), e);
    }

    return result;
  }

  private void saveExecutionLog(TargetServer server, HealthCheckResult result, String batchExecutionId) {
    try {
      ExecutionLog log = new ExecutionLog();
      log.setTargetServerId(server.getId());
      log.setServerName(server.getName());
      log.setUrl(server.getUrl());
      log.setMethod(server.getMethod());
      log.setSuccess(result.isSuccess());
      log.setStatusCode(result.getStatusCode());
      log.setElapsedTimeMs(result.getElapsedTime());
      log.setErrorMessage(result.getErrorMessage());
      log.setResponseBody(result.getResponse());
      log.setBatchExecutionId(batchExecutionId);
      log.setEnvironment(activeProfile);
      log.setExecutionTime(LocalDateTime.now());

      executionLogRepository.save(log);

    } catch (Exception e) {
      logger.error("Failed to save execution log for server {}: {}",
          server.getName(), e.getMessage(), e);
    }
  }

  public List<TargetServer> getActiveServers() {
    return targetServerRepository.findActiveServersByEnvironment(activeProfile);
  }

  public List<ExecutionLog> getRecentLogs(int hours) {
    LocalDateTime since = LocalDateTime.now().minusHours(hours);
    return executionLogRepository.findRecentLogs(since);
  }

  public List<ExecutionLog> getRecentFailures(int hours) {
    LocalDateTime since = LocalDateTime.now().minusHours(hours);
    return executionLogRepository.findRecentFailures(since);
  }

  public List<ExecutionLog> getSlowResponses(int hours, long thresholdMs) {
    LocalDateTime since = LocalDateTime.now().minusHours(hours);
    return executionLogRepository.findSlowResponses(thresholdMs, since);
  }
}
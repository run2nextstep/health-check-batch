package com.kica.ess.batch.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "execution_logs")
public class ExecutionLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "target_server_id")
  private Long targetServerId;

  @Column(name = "server_name", length = 100)
  private String serverName;

  @Column(name = "url", length = 500)
  private String url;

  @Column(name = "method", length = 10)
  private String method;

  @Column(name = "success")
  private Boolean success;

  @Column(name = "status_code")
  private Integer statusCode;

  @Column(name = "elapsed_time_ms")
  private Long elapsedTimeMs;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "response_body", columnDefinition = "TEXT")
  private String responseBody;

  @Column(name = "execution_time")
  private LocalDateTime executionTime;

  @Column(name = "batch_execution_id")
  private String batchExecutionId;

  @Column(name = "environment", length = 20)
  private String environment;

  @PrePersist
  protected void onCreate() {
    if (executionTime == null) {
      executionTime = LocalDateTime.now();
    }
  }

  // Constructors
  public ExecutionLog() {}

  public ExecutionLog(Long targetServerId, String serverName, String url, String method) {
    this.targetServerId = targetServerId;
    this.serverName = serverName;
    this.url = url;
    this.method = method;
    this.executionTime = LocalDateTime.now();
  }

  // Getters and Setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Long getTargetServerId() { return targetServerId; }
  public void setTargetServerId(Long targetServerId) { this.targetServerId = targetServerId; }

  public String getServerName() { return serverName; }
  public void setServerName(String serverName) { this.serverName = serverName; }

  public String getUrl() { return url; }
  public void setUrl(String url) { this.url = url; }

  public String getMethod() { return method; }
  public void setMethod(String method) { this.method = method; }

  public Boolean getSuccess() { return success; }
  public void setSuccess(Boolean success) { this.success = success; }

  public Integer getStatusCode() { return statusCode; }
  public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }

  public Long getElapsedTimeMs() { return elapsedTimeMs; }
  public void setElapsedTimeMs(Long elapsedTimeMs) { this.elapsedTimeMs = elapsedTimeMs; }

  public String getErrorMessage() { return errorMessage; }
  public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

  public String getResponseBody() { return responseBody; }
  public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

  public LocalDateTime getExecutionTime() { return executionTime; }
  public void setExecutionTime(LocalDateTime executionTime) { this.executionTime = executionTime; }

  public String getBatchExecutionId() { return batchExecutionId; }
  public void setBatchExecutionId(String batchExecutionId) { this.batchExecutionId = batchExecutionId; }

  public String getEnvironment() { return environment; }
  public void setEnvironment(String environment) { this.environment = environment; }

  public boolean isSlowResponse(long thresholdMs) {
    return elapsedTimeMs != null && elapsedTimeMs > thresholdMs;
  }

  @Override
  public String toString() {
    return String.format("ExecutionLog{id=%d, serverName='%s', success=%s, elapsedTimeMs=%d, executionTime=%s}",
        id, serverName, success, elapsedTimeMs, executionTime);
  }
}
package com.kica.ess.batch.dto;

import com.fasterxml.jackson.databind.JsonNode;

public class HealthCheckResult {

  private String serverName;
  private String url;
  private String method;
  private long startTime;
  private long endTime;
  private long elapsedTime;
  private boolean success;
  private String response;
  private JsonNode responseJson;
  private String errorMessage;
  private int statusCode;

  // Constructors
  public HealthCheckResult() {}

  public HealthCheckResult(String serverName, String url) {
    this.serverName = serverName;
    this.url = url;
  }

  // Getters and Setters
  public String getServerName() { return serverName; }
  public void setServerName(String serverName) { this.serverName = serverName; }

  public String getUrl() { return url; }
  public void setUrl(String url) { this.url = url; }

  public String getMethod() { return method; }
  public void setMethod(String method) { this.method = method; }

  public long getStartTime() { return startTime; }
  public void setStartTime(long startTime) { this.startTime = startTime; }

  public long getEndTime() { return endTime; }
  public void setEndTime(long endTime) { this.endTime = endTime; }

  public long getElapsedTime() { return elapsedTime; }
  public void setElapsedTime(long elapsedTime) { this.elapsedTime = elapsedTime; }

  public boolean isSuccess() { return success; }
  public void setSuccess(boolean success) { this.success = success; }

  public String getResponse() { return response; }
  public void setResponse(String response) { this.response = response; }

  public JsonNode getResponseJson() { return responseJson; }
  public void setResponseJson(JsonNode responseJson) { this.responseJson = responseJson; }

  public String getErrorMessage() { return errorMessage; }
  public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

  public int getStatusCode() { return statusCode; }
  public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

  // Utility methods
  public boolean isSlowResponse(long thresholdMs) {
    return elapsedTime > thresholdMs;
  }

  @Override
  public String toString() {
    return String.format("HealthCheckResult{server='%s', url='%s', method='%s', " +
            "elapsedTime=%dms, success=%s, statusCode=%d}",
        serverName, url, method, elapsedTime, success, statusCode);
  }
}
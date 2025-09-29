package com.kica.ess.batch.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "target_servers")
public class TargetServer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "url", nullable = false, length = 500)
  private String url;

  @Column(name = "method", nullable = false, length = 10)
  private String method = "GET";

  @Column(name = "timeout_ms")
  private Long timeout = 5000L;

  @Column(name = "request_body", columnDefinition = "TEXT")
  private String requestBody;

  @Column(name = "enabled")
  private Boolean enabled = true;

  @Column(name = "description", length = 500)
  private String description;

  @Column(name = "environment", length = 20)
  private String environment;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  // Constructors
  public TargetServer() {}

  public TargetServer(String name, String url, String method) {
    this.name = name;
    this.url = url;
    this.method = method;
  }

  // Getters and Setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getUrl() { return url; }
  public void setUrl(String url) { this.url = url; }

  public String getMethod() { return method; }
  public void setMethod(String method) { this.method = method; }

  public Long getTimeout() { return timeout; }
  public void setTimeout(Long timeout) { this.timeout = timeout; }

  public String getRequestBody() { return requestBody; }
  public void setRequestBody(String requestBody) { this.requestBody = requestBody; }

  public Boolean getEnabled() { return enabled; }
  public void setEnabled(Boolean enabled) { this.enabled = enabled; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public String getEnvironment() { return environment; }
  public void setEnvironment(String environment) { this.environment = environment; }

  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

  public LocalDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

  @Override
  public String toString() {
    return String.format("TargetServer{id=%d, name='%s', url='%s', method='%s', enabled=%s}",
        id, name, url, method, enabled);
  }
}
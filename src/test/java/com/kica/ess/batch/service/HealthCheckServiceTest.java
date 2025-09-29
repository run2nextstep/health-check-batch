package com.kica.ess.batch.service;

import com.kica.ess.batch.config.BatchProperties;
import com.kica.ess.batch.dto.HealthCheckResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthCheckServiceTest {

  @Mock
  private BatchProperties batchProperties;

  @Mock
  private WebClient webClient;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

  @Mock
  private WebClient.RequestHeadersSpec requestHeadersSpec;

  @Mock
  private WebClient.ResponseSpec responseSpec;

  @InjectMocks
  private HealthCheckService healthCheckService;

  @BeforeEach
  void setUp() {
    // Setup mock target servers
    BatchProperties.TargetServer server1 = new BatchProperties.TargetServer();
    server1.setName("Test Server 1");
    server1.setUrl("http://test1.com/health");
    server1.setMethod("GET");
    server1.setTimeout(5000);

    BatchProperties.TargetServer server2 = new BatchProperties.TargetServer();
    server2.setName("Test Server 2");
    server2.setUrl("http://test2.com/status");
    server2.setMethod("POST");
    server2.setTimeout(3000);
    server2.setBody("{\"service\":\"test\"}");

    when(batchProperties.getTargetServers()).thenReturn(Arrays.asList(server1, server2));
  }

  @Test
  void testSuccessfulHealthCheck() {
    // Arrange
    String mockResponse = "{\"status\":\"healthy\",\"version\":\"1.0\"}";

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockResponse));

    when(webClient.post()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.body(any())).thenReturn(requestHeadersSpec);

    try {
      when(objectMapper.readTree(mockResponse)).thenReturn(objectMapper.createObjectNode());
    } catch (Exception e) {
      // Mock exception handling
    }

    // Act
    List<HealthCheckResult> results = healthCheckService.performHealthCheck();

    // Assert
    assertNotNull(results);
    assertEquals(2, results.size());

    HealthCheckResult result1 = results.get(0);
    assertEquals("Test Server 1", result1.getServerName());
    assertEquals("http://test1.com/health", result1.getUrl());
    assertEquals("GET", result1.getMethod());
    assertTrue(result1.isSuccess());
    assertNotNull(result1.getResponse());

    HealthCheckResult result2 = results.get(1);
    assertEquals("Test Server 2", result2.getServerName());
    assertEquals("http://test2.com/status", result2.getUrl());
    assertEquals("POST", result2.getMethod());
    assertTrue(result2.isSuccess());
  }

  @Test
  void testFailedHealthCheck() {
    // Arrange
    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("Connection timeout")));

    when(webClient.post()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.body(any())).thenReturn(requestHeadersSpec);

    // Act
    List<HealthCheckResult> results = healthCheckService.performHealthCheck();

    // Assert
    assertNotNull(results);
    assertEquals(2, results.size());

    for (HealthCheckResult result : results) {
      assertFalse(result.isSuccess());
      assertNotNull(result.getErrorMessage());
      assertTrue(result.getErrorMessage().contains("Connection timeout"));
    }
  }

  @Test
  void testSlowResponse() {
    // Arrange
    String mockResponse = "{\"status\":\"healthy\"}";
    long slowDelay = 2000; // 2 seconds

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(String.class)).thenReturn(
        Mono.just(mockResponse).delayElement(java.time.Duration.ofMillis(slowDelay))
    );

    when(webClient.post()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.body(any())).thenReturn(requestHeadersSpec);

    // Act
    List<HealthCheckResult> results = healthCheckService.performHealthCheck();

    // Assert
    assertNotNull(results);
    assertEquals(2, results.size());

    for (HealthCheckResult result : results) {
      assertTrue(result.isSuccess());
      assertTrue(result.getElapsedTime() >= slowDelay);
      assertTrue(result.isSlowResponse(1000)); // 1 second threshold
    }
  }
}
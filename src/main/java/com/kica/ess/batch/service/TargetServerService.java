package com.kica.ess.batch.service;

import com.kica.ess.batch.entity.TargetServer;
import com.kica.ess.batch.repository.TargetServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TargetServerService {

  private static final Logger logger = LoggerFactory.getLogger(TargetServerService.class);

  @Autowired
  private TargetServerRepository targetServerRepository;

  @Value("${spring.profiles.active:dev}")
  private String activeProfile;

  @PostConstruct
  public void initializeDefaultServers() {
    if (targetServerRepository.count() == 0) {
      logger.info("Initializing default target servers for environment: {}", activeProfile);
      createDefaultServers();
    }
  }

  private void createDefaultServers() {
    // Create default servers based on environment
    if ("dev".equals(activeProfile)) {
      createServer("Local API Server", "http://localhost:8080/api/health", "GET", 5000L,
          "Development API health endpoint", activeProfile, null);
      createServer("Local Service", "http://localhost:8081/api/status", "POST", 3000L,
          "Development service status check", activeProfile, "{\"service\":\"health-check\"}");
    } else if ("test".equals(activeProfile)) {
      createServer("Test API Server", "http://test.internal.com/health", "GET", 3000L,
          "Test environment API", activeProfile, null);
    } else if ("prod".equals(activeProfile)) {
      createServer("Production API", "https://api.production.com/health", "GET", 5000L,
          "Production API health check", activeProfile, null);
      createServer("Payment Service", "https://payment.production.com/status", "POST", 8000L,
          "Production payment service", activeProfile, "{\"service\":\"payment\",\"check\":\"health\"}");
    }
  }

  public TargetServer createServer(String name, String url, String method, Long timeout,
                                   String description, String environment, String requestBody) {
    TargetServer server = new TargetServer();
    server.setName(name);
    server.setUrl(url);
    server.setMethod(method);
    server.setTimeout(timeout);
    server.setDescription(description);
    server.setEnvironment(environment);
    server.setRequestBody(requestBody);
    server.setEnabled(true);

    TargetServer saved = targetServerRepository.save(server);
    logger.info("Created target server: {}", saved);
    return saved;
  }

  public List<TargetServer> getAllServers() {
    return targetServerRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
  }

  public List<TargetServer> getActiveServers() {
    return targetServerRepository.findActiveServersByEnvironment(activeProfile);
  }

  public List<TargetServer> getServersByEnvironment(String environment) {
    return targetServerRepository.findByEnvironment(environment);
  }

  public Optional<TargetServer> getServerById(Long id) {
    return targetServerRepository.findById(id);
  }

  public TargetServer saveServer(TargetServer server) {
    if (server.getEnvironment() == null) {
      server.setEnvironment(activeProfile);
    }

    TargetServer saved = targetServerRepository.save(server);
    logger.info("Saved target server: {}", saved);
    return saved;
  }

  public TargetServer updateServer(Long id, TargetServer serverDetails) {
    Optional<TargetServer> optionalServer = targetServerRepository.findById(id);
    if (optionalServer.isPresent()) {
      TargetServer server = optionalServer.get();
      server.setName(serverDetails.getName());
      server.setUrl(serverDetails.getUrl());
      server.setMethod(serverDetails.getMethod());
      server.setTimeout(serverDetails.getTimeout());
      server.setRequestBody(serverDetails.getRequestBody());
      server.setDescription(serverDetails.getDescription());
      server.setEnvironment(serverDetails.getEnvironment());
      server.setEnabled(serverDetails.getEnabled());

      TargetServer updated = targetServerRepository.save(server);
      logger.info("Updated target server: {}", updated);
      return updated;
    }
    throw new RuntimeException("Target server not found with id: " + id);
  }

  public void deleteServer(Long id) {
    if (targetServerRepository.existsById(id)) {
      targetServerRepository.deleteById(id);
      logger.info("Deleted target server with id: {}", id);
    } else {
      throw new RuntimeException("Target server not found with id: " + id);
    }
  }

  public void toggleServerStatus(Long id) {
    Optional<TargetServer> optionalServer = targetServerRepository.findById(id);
    if (optionalServer.isPresent()) {
      TargetServer server = optionalServer.get();
      server.setEnabled(!server.getEnabled());
      targetServerRepository.save(server);
      logger.info("Toggled server {} status to: {}", server.getName(), server.getEnabled());
    } else {
      throw new RuntimeException("Target server not found with id: " + id);
    }
  }

  public List<TargetServer> searchServers(String searchTerm) {
    if (searchTerm == null || searchTerm.trim().isEmpty()) {
      return getAllServers();
    }

    List<TargetServer> nameResults = targetServerRepository.findByNameContainingIgnoreCase(searchTerm);
    List<TargetServer> urlResults = targetServerRepository.findByUrlContainingIgnoreCase(searchTerm);

    // Combine results without duplicates
    nameResults.addAll(urlResults);
    return nameResults.stream().distinct().collect(java.util.stream.Collectors.toList());
  }

  public boolean existsByNameAndEnvironment(String name, String environment) {
    return targetServerRepository.existsByNameAndEnvironment(name, environment);
  }

  public long getActiveServerCount() {
    return targetServerRepository.countActiveServersByEnvironment(activeProfile);
  }

  public long getTotalServerCount() {
    return targetServerRepository.count();
  }
}
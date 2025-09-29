package com.kica.ess.batch.controller;

import com.kica.ess.batch.entity.ExecutionLog;
import com.kica.ess.batch.entity.TargetServer;
import com.kica.ess.batch.service.TargetServerService;
import com.kica.ess.batch.repository.ExecutionLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/console")
public class WebConsoleController {

  private static final Logger logger = LoggerFactory.getLogger(WebConsoleController.class);

  @Autowired
  private TargetServerService targetServerService;

  @Autowired
  private ExecutionLogRepository executionLogRepository;

  @Value("${spring.profiles.active:dev}")
  private String activeProfile;

  @GetMapping({"", "/"})
  public String dashboard(Model model) {
    logger.info("Dashboard accessed - activeProfile: {}", activeProfile);

    try {
      model.addAttribute("activeProfile", activeProfile);
      model.addAttribute("totalServers", targetServerService.getTotalServerCount());
      model.addAttribute("activeServers", targetServerService.getActiveServerCount());

      // Recent stats
      LocalDateTime since24h = LocalDateTime.now().minusHours(24);
      model.addAttribute("successCount24h", executionLogRepository.countSuccessfulExecutions(since24h));
      model.addAttribute("failureCount24h", executionLogRepository.countFailedExecutions(since24h));

      Double avgResponseTime = executionLogRepository.getAverageResponseTime(since24h);
      model.addAttribute("avgResponseTime24h", avgResponseTime != null ? avgResponseTime.longValue() : 0);

      // Recent logs (limit to 10 for dashboard)
      model.addAttribute("recentLogs", executionLogRepository.findRecentLogs(since24h).stream().limit(10).collect(java.util.stream.Collectors.toList()));

      logger.info("Dashboard data loaded successfully");
      return "console/dashboard";

    } catch (Exception e) {
      logger.error("Error loading dashboard", e);
      model.addAttribute("error", "Failed to load dashboard: " + e.getMessage());
      return "console/dashboard";
    }
  }

  @GetMapping("/test")
  public String test(Model model) {
    logger.info("Test page accessed");
    model.addAttribute("message", "Web console is working!");
    model.addAttribute("activeProfile", activeProfile);
    return "console/test";
  }

  @GetMapping("/servers")
  public String listServers(Model model, @RequestParam(value = "search", required = false) String search) {
    logger.info("Servers page accessed with search: {}", search);

    try {
      if (search != null && !search.trim().isEmpty()) {
        model.addAttribute("servers", targetServerService.searchServers(search));
        model.addAttribute("search", search);
      } else {
        model.addAttribute("servers", targetServerService.getAllServers());
      }
      model.addAttribute("activeProfile", activeProfile);

      logger.info("Servers page loaded successfully");
      return "console/servers";

    } catch (Exception e) {
      logger.error("Error loading servers page", e);
      model.addAttribute("error", "Failed to load servers: " + e.getMessage());
      return "console/servers";
    }
  }

  @GetMapping("/servers/new")
  public String newServerForm(Model model) {
    logger.info("New server form accessed");
    model.addAttribute("server", new TargetServer());
    model.addAttribute("activeProfile", activeProfile);
    return "console/server-form";
  }

  @GetMapping("/servers/{id}/edit")
  public String editServerForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
    logger.info("Edit server form accessed for id: {}", id);

    Optional<TargetServer> server = targetServerService.getServerById(id);
    if (server.isPresent()) {
      model.addAttribute("server", server.get());
      model.addAttribute("activeProfile", activeProfile);
      return "console/server-form";
    } else {
      redirectAttributes.addFlashAttribute("error", "Server not found");
      return "redirect:/console/servers";
    }
  }

  @PostMapping("/servers")
  public String saveServer(@ModelAttribute TargetServer server, RedirectAttributes redirectAttributes) {
    logger.info("Saving new server: {}", server.getName());

    try {
      targetServerService.saveServer(server);
      redirectAttributes.addFlashAttribute("success", "Server saved successfully");
    } catch (Exception e) {
      logger.error("Failed to save server", e);
      redirectAttributes.addFlashAttribute("error", "Failed to save server: " + e.getMessage());
    }
    return "redirect:/console/servers";
  }

  @PostMapping("/servers/{id}/update")
  public String updateServer(@PathVariable Long id, @ModelAttribute TargetServer server,
                             RedirectAttributes redirectAttributes) {
    logger.info("Updating server id: {}", id);

    try {
      targetServerService.updateServer(id, server);
      redirectAttributes.addFlashAttribute("success", "Server updated successfully");
    } catch (Exception e) {
      logger.error("Failed to update server", e);
      redirectAttributes.addFlashAttribute("error", "Failed to update server: " + e.getMessage());
    }
    return "redirect:/console/servers";
  }

  @PostMapping("/servers/{id}/delete")
  public String deleteServer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    logger.info("Deleting server id: {}", id);

    try {
      targetServerService.deleteServer(id);
      redirectAttributes.addFlashAttribute("success", "Server deleted successfully");
    } catch (Exception e) {
      logger.error("Failed to delete server", e);
      redirectAttributes.addFlashAttribute("error", "Failed to delete server: " + e.getMessage());
    }
    return "redirect:/console/servers";
  }

  @PostMapping("/servers/{id}/toggle")
  public String toggleServerStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    logger.info("Toggling server status for id: {}", id);

    try {
      targetServerService.toggleServerStatus(id);
      redirectAttributes.addFlashAttribute("success", "Server status updated successfully");
    } catch (Exception e) {
      logger.error("Failed to update server status", e);
      redirectAttributes.addFlashAttribute("error", "Failed to update server status: " + e.getMessage());
    }
    return "redirect:/console/servers";
  }

  @GetMapping("/logs")
  public String viewLogs(Model model,
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         @RequestParam(value = "size", defaultValue = "20") int size,
                         @RequestParam(value = "success", required = false) Boolean success,
                         @RequestParam(value = "serverName", required = false) String serverName) {

    logger.info("Logs page accessed - page: {}, success: {}, serverName: {}", page, success, serverName);

    try {
      Pageable pageable = PageRequest.of(page, size);
      Page<ExecutionLog> logsPage;

      if (success != null) {
        logsPage = executionLogRepository.findBySuccessOrderByExecutionTimeDesc(success, pageable);
        model.addAttribute("successFilter", success);
      } else if (serverName != null && !serverName.trim().isEmpty()) {
        logsPage = executionLogRepository.findByServerNameContainingIgnoreCaseOrderByExecutionTimeDesc(serverName, pageable);
        model.addAttribute("serverNameFilter", serverName);
      } else {
        logsPage = executionLogRepository.findByOrderByExecutionTimeDesc(pageable);
      }

      model.addAttribute("logsPage", logsPage);
      model.addAttribute("activeProfile", activeProfile);

      logger.info("Logs page loaded successfully with {} entries", logsPage.getTotalElements());
      return "console/logs";

    } catch (Exception e) {
      logger.error("Error loading logs page", e);
      model.addAttribute("error", "Failed to load logs: " + e.getMessage());
      return "console/logs";
    }
  }

  @GetMapping("/logs/{id}")
  public String viewLogDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
    logger.info("Log detail accessed for id: {}", id);

    Optional<ExecutionLog> log = executionLogRepository.findById(id);
    if (log.isPresent()) {
      model.addAttribute("log", log.get());
      return "console/log-detail";
    } else {
      redirectAttributes.addFlashAttribute("error", "Log not found");
      return "redirect:/console/logs";
    }
  }

  @GetMapping("/servers/{id}/logs")
  public String viewServerLogs(@PathVariable Long id, Model model,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "20") int size,
                               RedirectAttributes redirectAttributes) {

    logger.info("Server logs accessed for server id: {}", id);

    Optional<TargetServer> server = targetServerService.getServerById(id);
    if (server.isPresent()) {
      Pageable pageable = PageRequest.of(page, size);
      Page<ExecutionLog> logsPage = executionLogRepository.findByTargetServerIdOrderByExecutionTimeDesc(id, pageable);

      model.addAttribute("server", server.get());
      model.addAttribute("logsPage", logsPage);
      return "console/server-logs";
    } else {
      redirectAttributes.addFlashAttribute("error", "Server not found");
      return "redirect:/console/servers";
    }
  }
}
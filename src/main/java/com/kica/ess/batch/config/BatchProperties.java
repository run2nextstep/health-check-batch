package com.kica.ess.batch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "batch.health-check")
public class BatchProperties {

  private List<TargetServer> targetServers;
  private Schedule schedule;
  private Timeout timeout;
  private Telegram telegram;

  public static class TargetServer {
    private String name;
    private String url;
    private String method = "GET";
    private long timeout = 5000;
    private String body;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public long getTimeout() { return timeout; }
    public void setTimeout(long timeout) { this.timeout = timeout; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
  }

  public static class Schedule {
    private String cron;

    public String getCron() { return cron; }
    public void setCron(String cron) { this.cron = cron; }
  }

  public static class Timeout {
    private long threshold;

    public long getThreshold() { return threshold; }
    public void setThreshold(long threshold) { this.threshold = threshold; }
  }

  public static class Telegram {
    private String botToken;
    private String chatId;
    private boolean enabled = false;

    public String getBotToken() { return botToken; }
    public void setBotToken(String botToken) { this.botToken = botToken; }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
  }

  // Main class getters and setters
  public List<TargetServer> getTargetServers() { return targetServers; }
  public void setTargetServers(List<TargetServer> targetServers) { this.targetServers = targetServers; }

  public Schedule getSchedule() { return schedule; }
  public void setSchedule(Schedule schedule) { this.schedule = schedule; }

  public Timeout getTimeout() { return timeout; }
  public void setTimeout(Timeout timeout) { this.timeout = timeout; }

  public Telegram getTelegram() { return telegram; }
  public void setTelegram(Telegram telegram) { this.telegram = telegram; }
}
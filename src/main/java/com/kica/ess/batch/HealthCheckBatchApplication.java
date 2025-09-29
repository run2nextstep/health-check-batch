package com.kica.ess.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class HealthCheckBatchApplication {

  public static void main(String[] args) {
    SpringApplication.run(HealthCheckBatchApplication.class, args);
  }
}
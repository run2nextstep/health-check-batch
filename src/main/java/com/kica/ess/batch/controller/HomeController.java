package com.kica.ess.batch.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

  private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

  @GetMapping("/")
  public String home() {
    logger.info("Root path accessed - redirecting to console");
    return "redirect:/console/";
  }

  @GetMapping("/index")
  public String index() {
    logger.info("Index path accessed - redirecting to console");
    return "redirect:/console/";
  }

  @GetMapping("/home")
  public String homePage() {
    logger.info("Home path accessed - redirecting to console");
    return "redirect:/console/";
  }
}
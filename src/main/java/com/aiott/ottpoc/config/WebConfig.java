package com.aiott.ottpoc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/hls/**")
        .addResourceLocations("file:./data/hls/");
    registry.addResourceHandler("/thumbnails/**")
        .addResourceLocations("file:./data/thumbnails/");
    registry.addResourceHandler("/stream/**")
        .addResourceLocations("file:./data/hls/");
  }
}

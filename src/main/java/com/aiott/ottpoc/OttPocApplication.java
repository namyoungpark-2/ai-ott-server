package com.aiott.ottpoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class OttPocApplication {

	public static void main(String[] args) {
		SpringApplication.run(OttPocApplication.class, args);
	}

}

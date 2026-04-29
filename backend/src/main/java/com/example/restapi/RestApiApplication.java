package com.example.restapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestApiApplication {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RestApiApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(RestApiApplication.class, args);
		log.info("API is running...");
		log.info("Confirmation");
	}

}


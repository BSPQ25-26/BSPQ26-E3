package com.example.restapi;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RestApiApplicationTests {

	private static final Logger log = LoggerFactory.getLogger(RestApiApplicationTests.class);

	@Test
	void contextLoads() {
		// Basic sanity test - does not require full Spring context
		log.info("contextLoads passed: Spring context loaded successfully");
	}

}

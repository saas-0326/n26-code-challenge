package com.n26.challenge.application;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Statistics API application - Spring Boot application configuration class
 *
 * @author Santiago Alzate S.
 *
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = { "com.n26.challenge.api.controller", "com.n26.challenge.service" })
public class StatisticsApiApplication {

	/**
	 * Starts the Statistics API Application.
	 *
	 * @param args Application parameters
	 */
	public static void main(final String[] args) {

		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(StatisticsApiApplication.class, args);

	}
	
}

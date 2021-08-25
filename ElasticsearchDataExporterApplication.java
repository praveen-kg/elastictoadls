package com.elk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * Main class for Springboot 
 */
@SpringBootApplication
@EnableScheduling
@EnableRetry

public class FraudDataExporterApplication{

    /**
     * Springboot main execution method
     * @param args runtime arguments
     */
	public static void main(String[] args) {
		SpringApplication.run(FraudDataExporterApplication.class, args);
	}
	
		
}

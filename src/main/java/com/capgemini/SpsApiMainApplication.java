package com.capgemini;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan( basePackages = {"com.capgemini.model"} )
@EnableAutoConfiguration
public class SpsApiMainApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpsApiMainApplication.class, args);
	}
}

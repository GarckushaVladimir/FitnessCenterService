package com.fitness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.fitness.repository")
public class FitnessCenterServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FitnessCenterServiceApplication.class, args);
	}

}


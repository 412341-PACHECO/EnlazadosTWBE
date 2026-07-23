package com.example.EnlazadosTW;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.example.EnlazadosTW.repositories")
public class EnlazadosTwApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnlazadosTwApplication.class, args);
	}

}

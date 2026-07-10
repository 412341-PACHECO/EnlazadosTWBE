package com.example.EnlazadosTW;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.EnlazadosTW.repositories")
public class EnlazadosTwApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnlazadosTwApplication.class, args);
	}

}

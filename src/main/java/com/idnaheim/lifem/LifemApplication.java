package com.idnaheim.lifem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LifemApplication {

	public static void main(String[] args) {
		SpringApplication.run(LifemApplication.class, args);
	}

}

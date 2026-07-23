package com.rms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing

public class StaySphereBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(StaySphereBackendApplication.class, args);
	}

}

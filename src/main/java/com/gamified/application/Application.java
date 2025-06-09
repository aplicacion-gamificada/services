package com.gamified.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	static {
		// Configurar el archivo de propiedades de logging
		System.setProperty("java.util.logging.config.file", "src/main/resources/logging.properties");
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}

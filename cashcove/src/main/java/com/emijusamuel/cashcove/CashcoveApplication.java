package com.emijusamuel.cashcove;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.github.cdimascio.dotenv.Dotenv;

@EnableScheduling
@SpringBootApplication
public class CashcoveApplication {

	public static void main(String[] args) {
		// Load .env file before Spring starts
		Dotenv dotenv = Dotenv.configure()
				.directory("./cashcove")
				.filename(".env")
				.ignoreIfMissing()
				.load();
		
		// Set environment variables as system properties
		dotenv.entries().forEach(entry -> 
			System.setProperty(entry.getKey(), entry.getValue())
		);
		
		SpringApplication.run(CashcoveApplication.class, args);
	}

}

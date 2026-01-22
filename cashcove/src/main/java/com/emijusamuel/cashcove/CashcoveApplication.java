package com.emijusamuel.cashcove;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CashcoveApplication {

	public static void main(String[] args) {
		SpringApplication.run(CashcoveApplication.class, args);
	}

}

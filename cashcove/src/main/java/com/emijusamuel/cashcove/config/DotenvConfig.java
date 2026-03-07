package com.emijusamuel.cashcove.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotenvConfig {

    static {
        Dotenv dotenv = Dotenv.configure()
                .directory("./cashcove")
                .filename(".env")
                .ignoreIfMissing()
                .load();
        
        // Set environment variables from .env file
        dotenv.entries().forEach(entry -> 
            System.setProperty(entry.getKey(), entry.getValue())
        );
    }
}

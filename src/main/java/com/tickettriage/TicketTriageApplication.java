package com.tickettriage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TicketTriageApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketTriageApplication.class, args);
    }
}

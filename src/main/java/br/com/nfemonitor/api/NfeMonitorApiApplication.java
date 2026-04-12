package br.com.nfemonitor.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NfeMonitorApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NfeMonitorApiApplication.class, args);
    }
} 
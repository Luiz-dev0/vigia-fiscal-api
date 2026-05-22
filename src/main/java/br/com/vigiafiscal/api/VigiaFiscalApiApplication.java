package br.com.vigiafiscal.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VigiaFiscalApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(VigiaFiscalApiApplication.class, args);
    }
}
package com.equinos.dieta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Microservicio dieta: catálogo NRC, cálculo de la ración y dietas guardadas. Puerto 8083. */
@SpringBootApplication
public class DietaApplication {

    public static void main(String[] args) {
        SpringApplication.run(DietaApplication.class, args);
    }
}

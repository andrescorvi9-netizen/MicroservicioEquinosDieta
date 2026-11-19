package com.equinos.registrocaballo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Microservicio registro-caballo: caballos de cada usuario. Puerto 8082. */
@SpringBootApplication
public class RegistroCaballoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RegistroCaballoApplication.class, args);
    }
}

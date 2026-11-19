package com.equinos.dieta.application.config;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Hilos para avisar a notificaciones sin retrasar la respuesta al usuario. */
@Configuration
public class NotificacionesConfig {

    private static final Logger log = LoggerFactory.getLogger(NotificacionesConfig.class);

    /** La cola tiene tope: si se llena, el aviso se descarta y la operación del usuario no se afecta. */
    @Bean
    public Executor notificacionesExecutor() {
        return new ThreadPoolExecutor(1, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(200),
                (tarea, grupo) -> log.warn("Cola de avisos llena: se descartó una notificación"));
    }
}

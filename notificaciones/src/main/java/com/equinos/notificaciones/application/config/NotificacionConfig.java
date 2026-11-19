package com.equinos.notificaciones.application.config;

import com.equinos.notificaciones.domain.model.gateway.CorreoGateway;
import com.equinos.notificaciones.domain.usecase.NotificacionUseCase;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Crea el caso de uso y el grupo de hilos que envía los correos sin hacer esperar a quien llama. */
@Configuration
public class NotificacionConfig {

    private static final Logger log = LoggerFactory.getLogger(NotificacionConfig.class);

    @Bean
    public NotificacionUseCase notificacionUseCase(CorreoGateway correoGateway) {
        return new NotificacionUseCase(correoGateway);
    }

    /** La cola tiene tope: si se llena, la notificación se descarta y el servicio sigue respondiendo. */
    @Bean
    public Executor notificacionesExecutor() {
        return new ThreadPoolExecutor(2, 4, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(500),
                (tarea, grupo) -> log.warn("Cola de notificaciones llena: se descartó una notificación"));
    }
}

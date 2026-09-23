package com.buildzone.api.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Beans generales de la aplicacion. */
@Configuration
public class AppConfig {

    /**
     * Reloj de la aplicacion en la zona horaria de Colombia. Los servicios
     * lo reciben inyectado para que las pruebas puedan usar una fecha fija.
     */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Bogota"));
    }
}

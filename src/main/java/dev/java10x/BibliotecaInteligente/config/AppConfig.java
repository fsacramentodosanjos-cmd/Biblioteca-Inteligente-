package dev.java10x.BibliotecaInteligente.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    /** Relógio injetável: nos testes dá para fixar a data de "hoje". */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}

package br.gov.go.defensoria.portal.shared.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Relógio da aplicação no fuso de Goiás: idade do assistido e ano do protocolo usam a data local.
 * Injetável para que os testes fixem o "hoje".
 */
@Configuration
@EnableConfigurationProperties(DpeProperties.class)
public class ClockConfig {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    @Bean
    Clock clock() {
        return Clock.system(FUSO);
    }
}

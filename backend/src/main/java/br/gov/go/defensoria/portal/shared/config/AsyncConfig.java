package br.gov.go.defensoria.portal.shared.config;

import java.util.Map;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Habilita @Async no executor padrão do Spring Boot. O TaskDecorator copia o MDC
 * (ex.: X-Request-Id) para a thread assíncrona, para os logs do envio de e-mail
 * continuarem ligados à requisição que os originou.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    TaskDecorator mdcTaskDecorator() {
        return tarefa -> {
            Map<String, String> contexto = MDC.getCopyOfContextMap();
            return () -> {
                if (contexto != null) {
                    MDC.setContextMap(contexto);
                }
                try {
                    tarefa.run();
                } finally {
                    MDC.clear();
                }
            };
        };
    }
}

package br.gov.go.defensoria.portal.shared.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Parâmetros de negócio da Defensoria.
 *
 * @param salarioMinimo  salário mínimo vigente
 * @param limiteSalarios até quantos salários mínimos de renda individual a hipossuficiência é presumida
 */
@Validated
@ConfigurationProperties(prefix = "dpe")
public record DpeProperties(@NotNull @Positive BigDecimal salarioMinimo, @NotNull @Positive Integer limiteSalarios) {

    /** Renda individual máxima para hipossuficiência presumida (limiteSalarios x salarioMinimo). */
    public BigDecimal limiteRendaPresumida() {
        return salarioMinimo.multiply(BigDecimal.valueOf(limiteSalarios));
    }
}

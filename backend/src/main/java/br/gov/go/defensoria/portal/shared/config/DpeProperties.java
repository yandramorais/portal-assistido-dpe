package br.gov.go.defensoria.portal.shared.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Validated
@ConfigurationProperties(prefix = "dpe")
public record DpeProperties(@NotNull @Positive BigDecimal salarioMinimo, @NotNull @Positive Integer limiteSalarios) {

    public BigDecimal limiteRendaPresumida() {
        return salarioMinimo.multiply(BigDecimal.valueOf(limiteSalarios));
    }
}

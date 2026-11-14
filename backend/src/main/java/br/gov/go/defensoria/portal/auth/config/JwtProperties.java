package br.gov.go.defensoria.portal.auth.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "portal.jwt")
public record JwtProperties(String secret, @DefaultValue("8h") Duration expiracao) {
}

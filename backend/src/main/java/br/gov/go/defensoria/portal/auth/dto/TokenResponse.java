package br.gov.go.defensoria.portal.auth.dto;

import java.time.Instant;

import br.gov.go.defensoria.portal.auth.domain.Perfil;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token de acesso. Envie no header Authorization: Bearer {token}.")
public record TokenResponse(
        @Schema(example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.assinatura") String token,
        @Schema(example = "Bearer") String tipo,
        @Schema(example = "2026-11-15T02:00:00Z") Instant expiraEm,
        UsuarioLogado usuario) {

    public record UsuarioLogado(
            @Schema(example = "1") Long id,
            @Schema(example = "Carla Mendes (Servidora)") String nome,
            @Schema(example = "SERVIDOR") Perfil perfil) {
    }
}

package br.gov.go.defensoria.portal.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais de acesso")
public record LoginRequest(
        @Schema(example = "servidor@dpe.local") @NotBlank @Email String email,
        @Schema(example = "Senha@123") @NotBlank String senha) {
}

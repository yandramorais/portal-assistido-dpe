package br.gov.go.defensoria.portal.assistido.dto;

import java.math.BigDecimal;
import java.util.Set;

import br.gov.go.defensoria.portal.assistido.domain.Vulnerabilidade;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Atualização completa (PUT) dos dados que o próprio assistido pode alterar.
 * CPF, data de nascimento e e-mail de login não mudam por aqui.
 */
public record AtualizarAssistidoRequest(
        @NotBlank @Size(max = 150) String nome,
        @Size(max = 150) String nomeSocial,
        @NotBlank @Size(max = 20) String telefone,
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido") String cep,
        @Size(max = 150) String logradouro,
        @Size(max = 10) String numero,
        @Size(max = 80) String complemento,
        @Size(max = 80) String bairro,
        @NotBlank @Size(max = 80) String cidade,
        @NotBlank @Pattern(regexp = "[A-Za-z]{2}", message = "UF inválida") String uf,
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal rendaIndividual,
        @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal rendaFamiliar,
        @NotNull @Min(1) @Max(99) Short membrosFamilia,
        @NotNull Boolean possuiImovel,
        @NotNull Boolean possuiVeiculo,
        @NotNull Set<Vulnerabilidade> vulnerabilidades) {
}

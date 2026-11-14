package br.gov.go.defensoria.portal.auth.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import br.gov.go.defensoria.portal.assistido.domain.Vulnerabilidade;
import br.gov.go.defensoria.portal.shared.validation.Cpf;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Cadastro do assistido: cria o login (perfil ASSISTIDO) e a ficha socioeconômica. */
@Schema(description = "Cadastro do assistido (login e ficha socioeconômica)")
public record RegistroRequest(
        @Schema(example = "Ana Paula Ribeiro") @NotBlank @Size(max = 150) String nome,
        @Schema(description = "Nome pelo qual a pessoa quer ser chamada; aparece no lugar do nome civil")
        @Size(max = 150) String nomeSocial,
        @Schema(description = "Com ou sem máscara; os dígitos verificadores são conferidos",
                example = "529.982.247-25") @NotBlank @Cpf String cpf,
        @Schema(example = "1985-04-10") @NotNull @Past LocalDate dataNascimento,
        @Schema(example = "62999990000") @NotBlank @Size(max = 20) String telefone,
        @Schema(example = "ana.ribeiro@exemplo.com") @NotBlank @Email @Size(max = 150) String email,
        // BCrypt considera só os primeiros 72 bytes.
        @Schema(description = "De 8 a 72 caracteres", example = "SenhaForte#1")
        @NotBlank @Size(min = 8, max = 72) String senha,
        @Schema(example = "74000-000") @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido") String cep,
        @Schema(example = "Rua 10") @Size(max = 150) String logradouro,
        @Schema(example = "120") @Size(max = 10) String numero,
        @Schema(example = "Qd. 5, Lt. 3") @Size(max = 80) String complemento,
        @Schema(example = "Setor Central") @Size(max = 80) String bairro,
        @Schema(example = "Goiânia") @NotBlank @Size(max = 80) String cidade,
        @Schema(example = "GO") @Pattern(regexp = "[A-Za-z]{2}", message = "UF inválida") String uf,
        @Schema(description = "Renda mensal individual, em reais", example = "1800.00")
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal rendaIndividual,
        @Schema(description = "Renda mensal da família, em reais", example = "2900.00")
        @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal rendaFamiliar,
        @Schema(example = "3") @Min(1) @Max(99) Short membrosFamilia,
        @Schema(example = "false") Boolean possuiImovel,
        @Schema(example = "false") Boolean possuiVeiculo,
        @Schema(description = "Situações que dão prioridade ao atendimento", example = "[\"DOENCA_GRAVE\"]")
        Set<Vulnerabilidade> vulnerabilidades) {
}

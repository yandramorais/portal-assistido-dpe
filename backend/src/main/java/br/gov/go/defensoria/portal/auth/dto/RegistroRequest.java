package br.gov.go.defensoria.portal.auth.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import br.gov.go.defensoria.portal.assistido.domain.Vulnerabilidade;
import br.gov.go.defensoria.portal.shared.validation.Cpf;
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
public record RegistroRequest(
        @NotBlank @Size(max = 150) String nome,
        @Size(max = 150) String nomeSocial,
        @NotBlank @Cpf String cpf,
        @NotNull @Past LocalDate dataNascimento,
        @NotBlank @Size(max = 20) String telefone,
        @NotBlank @Email @Size(max = 150) String email,
        // BCrypt considera só os primeiros 72 bytes.
        @NotBlank @Size(min = 8, max = 72) String senha,
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido") String cep,
        @Size(max = 150) String logradouro,
        @Size(max = 10) String numero,
        @Size(max = 80) String complemento,
        @Size(max = 80) String bairro,
        @NotBlank @Size(max = 80) String cidade,
        @Pattern(regexp = "[A-Za-z]{2}", message = "UF inválida") String uf,
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal rendaIndividual,
        @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal rendaFamiliar,
        @Min(1) @Max(99) Short membrosFamilia,
        Boolean possuiImovel,
        Boolean possuiVeiculo,
        Set<Vulnerabilidade> vulnerabilidades) {
}

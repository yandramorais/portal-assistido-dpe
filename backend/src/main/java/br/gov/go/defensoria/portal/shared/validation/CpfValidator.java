package br.gov.go.defensoria.portal.shared.validation;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<Cpf, String> {

    private static final Pattern FORMATO = Pattern.compile("\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}");

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        return valor == null || isValido(valor);
    }

    public static boolean isValido(String cpf) {
        if (cpf == null || !FORMATO.matcher(cpf).matches()) {
            return false;
        }
        String digitos = somenteDigitos(cpf);
        if (digitos.chars().distinct().count() == 1) {
            return false;
        }
        return digitoVerificador(digitos, 9) == digitos.charAt(9) - '0'
                && digitoVerificador(digitos, 10) == digitos.charAt(10) - '0';
    }

    /** Remove máscara: "123.456.789-09" -> "12345678909". */
    public static String somenteDigitos(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    /** Dígito verificador calculado sobre os {@code quantidade} primeiros dígitos (pesos decrescentes até 2). */
    private static int digitoVerificador(String digitos, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (digitos.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}

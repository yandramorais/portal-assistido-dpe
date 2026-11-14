package br.gov.go.defensoria.portal.shared.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CpfValidatorTest {

    private final CpfValidator validator = new CpfValidator();

    @ParameterizedTest
    @ValueSource(strings = { "52998224725", "529.982.247-25", "52601815906", "62819482112", "11144477735" })
    void aceitaCpfValido(String cpf) {
        assertThat(validator.isValid(cpf, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { "52998224724", "52998224715", "529.982.247-26", "12345678900" })
    void recusaDigitoVerificadorErrado(String cpf) {
        assertThat(validator.isValid(cpf, null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = { "00000000000", "11111111111", "111.111.111-11", "99999999999" })
    void recusaDigitosRepetidos(String cpf) {
        assertThat(validator.isValid(cpf, null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "5299822472", "529982247250", "529.982.24725", "abc.def.ghi-jk" })
    void recusaFormatoInvalido(String cpf) {
        assertThat(validator.isValid(cpf, null)).isFalse();
    }

    @Test
    void nuloFicaParaONotBlank() {
        assertThat(validator.isValid(null, null)).isTrue();
    }
}

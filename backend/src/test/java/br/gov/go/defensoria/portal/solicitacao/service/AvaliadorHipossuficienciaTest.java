package br.gov.go.defensoria.portal.solicitacao.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.gov.go.defensoria.portal.shared.config.DpeProperties;
import br.gov.go.defensoria.portal.solicitacao.domain.Hipossuficiencia;

class AvaliadorHipossuficienciaTest {

    private final AvaliadorHipossuficiencia avaliador = new AvaliadorHipossuficiencia(
            new DpeProperties(new BigDecimal("1621.00"), 3));

    @ParameterizedTest
    @ValueSource(strings = { "0", "0.00", "1621.00", "4862.99", "4863.00", "4863" })
    void ateTresSalariosMinimosInclusiveEPresumida(String renda) {
        assertThat(avaliador.avaliar(new BigDecimal(renda))).isEqualTo(Hipossuficiencia.PRESUMIDA);
    }

    @ParameterizedTest
    @ValueSource(strings = { "4863.01", "6200.00", "100000" })
    void acimaDeTresSalariosMinimosRequerAnalise(String renda) {
        assertThat(avaliador.avaliar(new BigDecimal(renda))).isEqualTo(Hipossuficiencia.REQUER_ANALISE);
    }
}

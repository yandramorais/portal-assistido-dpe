package br.gov.go.defensoria.portal.solicitacao.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

import org.junit.jupiter.api.Test;

import br.gov.go.defensoria.portal.assistido.domain.Vulnerabilidade;
import br.gov.go.defensoria.portal.solicitacao.domain.Prioridade;

class CalculadoraPrioridadeTest {

    private static final ZoneId GOIANIA = ZoneId.of("America/Sao_Paulo");

    /** "Hoje" = 14/11/2026 ao meio-dia em Goiânia. */
    private final CalculadoraPrioridade calculadora = calculadoraEm("2026-11-14T15:00:00Z");

    @Test
    void cinquentaENoveAnosSemVulnerabilidadeENormal() {
        // Faz 60 amanhã.
        assertThat(calculadora.calcular(LocalDate.of(1966, 11, 15), Set.of())).isEqualTo(Prioridade.NORMAL);
    }

    @Test
    void completandoSessentaAnosHojeEAlta() {
        assertThat(calculadora.calcular(LocalDate.of(1966, 11, 14), Set.of())).isEqualTo(Prioridade.ALTA);
    }

    @Test
    void maisDeSessentaAnosEAlta() {
        assertThat(calculadora.calcular(LocalDate.of(1946, 6, 25), Set.of())).isEqualTo(Prioridade.ALTA);
    }

    @Test
    void qualquerVulnerabilidadeEAltaMesmoJovem() {
        for (Vulnerabilidade v : Vulnerabilidade.values()) {
            assertThat(calculadora.calcular(LocalDate.of(1999, 12, 1), Set.of(v))).isEqualTo(Prioridade.ALTA);
        }
    }

    @Test
    void jovemSemVulnerabilidadeENormal() {
        assertThat(calculadora.calcular(LocalDate.of(1999, 12, 1), Set.of())).isEqualTo(Prioridade.NORMAL);
        assertThat(calculadora.calcular(LocalDate.of(1999, 12, 1), null)).isEqualTo(Prioridade.NORMAL);
    }

    @Test
    void usaADataDeGoianiaENaoUtc() {
        // 02:00 UTC de 14/11 ainda é 13/11 em Goiânia: aniversário de 60 anos ainda não chegou.
        CalculadoraPrioridade madrugada = calculadoraEm("2026-11-14T02:00:00Z");
        assertThat(madrugada.calcular(LocalDate.of(1966, 11, 14), Set.of())).isEqualTo(Prioridade.NORMAL);
    }

    @Test
    void nascidoEm29DeFevereiro() {
        LocalDate nascimento = LocalDate.of(1964, 2, 29);
        assertThat(calculadoraEm("2024-02-28T15:00:00Z").calcular(nascimento, Set.of())).isEqualTo(Prioridade.NORMAL);
        assertThat(calculadoraEm("2024-02-29T15:00:00Z").calcular(nascimento, Set.of())).isEqualTo(Prioridade.ALTA);
    }

    private static CalculadoraPrioridade calculadoraEm(String instante) {
        return new CalculadoraPrioridade(Clock.fixed(Instant.parse(instante), GOIANIA));
    }
}

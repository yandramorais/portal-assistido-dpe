package br.gov.go.defensoria.portal.solicitacao.domain;

import static br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao.AGUARDANDO_TRIAGEM;
import static br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao.CONCLUIDO;
import static br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao.DEFERIDO;
import static br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao.EM_ANALISE;
import static br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao.ENCAMINHADO;
import static br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao.INDEFERIDO;
import static br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao.PENDENTE_DOCUMENTACAO;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class StatusSolicitacaoTest {

    private static final Set<String> TRANSICOES_VALIDAS = Set.of(
            "AGUARDANDO_TRIAGEM -> EM_ANALISE",
            "EM_ANALISE -> PENDENTE_DOCUMENTACAO",
            "EM_ANALISE -> DEFERIDO",
            "EM_ANALISE -> ENCAMINHADO",
            "EM_ANALISE -> INDEFERIDO",
            "PENDENTE_DOCUMENTACAO -> EM_ANALISE",
            "PENDENTE_DOCUMENTACAO -> INDEFERIDO",
            "DEFERIDO -> CONCLUIDO",
            "INDEFERIDO -> EM_ANALISE");

    static Stream<Arguments> todosOsPares() {
        return Arrays.stream(StatusSolicitacao.values())
                .flatMap(origem -> Arrays.stream(StatusSolicitacao.values())
                        .map(destino -> Arguments.of(origem, destino,
                                TRANSICOES_VALIDAS.contains(origem + " -> " + destino))));
    }

    @ParameterizedTest(name = "{0} -> {1}: {2}")
    @MethodSource("todosOsPares")
    void podeIrPara(StatusSolicitacao origem, StatusSolicitacao destino, boolean permitido) {
        assertThat(origem.podeIrPara(destino)).isEqualTo(permitido);
    }

    @ParameterizedTest
    @EnumSource(StatusSolicitacao.class)
    void nuncaVaiParaOProprioStatusNemParaNulo(StatusSolicitacao status) {
        assertThat(status.podeIrPara(status)).isFalse();
        assertThat(status.podeIrPara(null)).isFalse();
    }

    @Test
    void encaminhadoEConcluidoSaoFinais() {
        assertThat(Arrays.stream(StatusSolicitacao.values()).filter(StatusSolicitacao::isFinal))
                .containsExactlyInAnyOrder(ENCAMINHADO, CONCLUIDO);
    }

    @Test
    void destinosPossiveisBatemComATabela() {
        assertThat(AGUARDANDO_TRIAGEM.destinosPossiveis()).containsExactly(EM_ANALISE);
        assertThat(EM_ANALISE.destinosPossiveis())
                .containsExactlyInAnyOrder(PENDENTE_DOCUMENTACAO, DEFERIDO, ENCAMINHADO, INDEFERIDO);
        assertThat(PENDENTE_DOCUMENTACAO.destinosPossiveis()).containsExactlyInAnyOrder(EM_ANALISE, INDEFERIDO);
        assertThat(DEFERIDO.destinosPossiveis()).containsExactly(CONCLUIDO);
        assertThat(INDEFERIDO.destinosPossiveis()).containsExactly(EM_ANALISE);
    }
}

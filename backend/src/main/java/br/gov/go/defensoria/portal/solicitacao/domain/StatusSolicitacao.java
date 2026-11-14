package br.gov.go.defensoria.portal.solicitacao.domain;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatusSolicitacao {
    AGUARDANDO_TRIAGEM("Aguardando triagem"),
    EM_ANALISE("Em análise"),
    PENDENTE_DOCUMENTACAO("Pendente de documentação"),
    DEFERIDO("Deferido"),
    ENCAMINHADO("Encaminhado"),
    INDEFERIDO("Indeferido"),
    CONCLUIDO("Concluído");

    private static final Map<StatusSolicitacao, Set<StatusSolicitacao>> TRANSICOES =
            new EnumMap<>(StatusSolicitacao.class);

    static {
        TRANSICOES.put(AGUARDANDO_TRIAGEM, EnumSet.of(EM_ANALISE));
        TRANSICOES.put(EM_ANALISE, EnumSet.of(PENDENTE_DOCUMENTACAO, DEFERIDO, ENCAMINHADO, INDEFERIDO));
        TRANSICOES.put(PENDENTE_DOCUMENTACAO, EnumSet.of(EM_ANALISE, INDEFERIDO));
        TRANSICOES.put(DEFERIDO, EnumSet.of(CONCLUIDO));
        TRANSICOES.put(INDEFERIDO, EnumSet.of(EM_ANALISE));
        TRANSICOES.put(ENCAMINHADO, EnumSet.noneOf(StatusSolicitacao.class));
        TRANSICOES.put(CONCLUIDO, EnumSet.noneOf(StatusSolicitacao.class));
    }

    private final String descricao;

    public boolean podeIrPara(StatusSolicitacao destino) {
        return destino != null && TRANSICOES.get(this).contains(destino);
    }

    public Set<StatusSolicitacao> destinosPossiveis() {
        return Collections.unmodifiableSet(TRANSICOES.get(this));
    }

    public boolean isFinal() {
        return TRANSICOES.get(this).isEmpty();
    }
}

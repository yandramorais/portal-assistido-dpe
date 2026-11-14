package br.gov.go.defensoria.portal.notificacao.event;

import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;

public record SolicitacaoAtualizadaEvent(
        Long solicitacaoId,
        String protocolo,
        StatusSolicitacao statusAnterior,
        StatusSolicitacao statusNovo,
        String destinatario,
        boolean notificarAssistido) {
}

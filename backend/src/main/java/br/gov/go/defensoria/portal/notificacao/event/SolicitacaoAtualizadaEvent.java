package br.gov.go.defensoria.portal.notificacao.event;

import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;

/**
 * Status de um pedido mudou. Carrega só o necessário para avisar o assistido:
 * nada de relato nem dados pessoais além do e-mail de destino.
 *
 * @param destinatario       e-mail do assistido (pode ser nulo)
 * @param notificarAssistido falso quando a mudança partiu do próprio assistido
 */
public record SolicitacaoAtualizadaEvent(
        Long solicitacaoId,
        String protocolo,
        StatusSolicitacao statusAnterior,
        StatusSolicitacao statusNovo,
        String destinatario,
        boolean notificarAssistido) {
}

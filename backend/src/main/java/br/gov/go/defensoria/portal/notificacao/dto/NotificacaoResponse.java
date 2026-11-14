package br.gov.go.defensoria.portal.notificacao.dto;

import java.time.Instant;

import br.gov.go.defensoria.portal.notificacao.domain.NotificacaoEmail;
import br.gov.go.defensoria.portal.notificacao.domain.StatusNotificacao;

public record NotificacaoResponse(
        Long id,
        String protocolo,
        String destinatario,
        String assunto,
        StatusNotificacao status,
        String erro,
        Instant criadoEm,
        Instant enviadoEm) {

    public static NotificacaoResponse de(NotificacaoEmail n) {
        return new NotificacaoResponse(n.getId(), n.getSolicitacao().getProtocolo(), n.getDestinatario(),
                n.getAssunto(), n.getStatus(), n.getErro(), n.getCriadoEm(), n.getEnviadoEm());
    }
}

package br.gov.go.defensoria.portal.solicitacao.dto;

import java.time.Instant;

import br.gov.go.defensoria.portal.auth.domain.Perfil;
import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;
import br.gov.go.defensoria.portal.workflow.domain.Mensagem;

public record MensagemResponse(
        Long id,
        String autor,
        boolean daEquipe,
        boolean visivelAssistido,
        String conteudo,
        Instant criadoEm,
        StatusSolicitacao statusSolicitacao) {

    public static MensagemResponse de(Mensagem m) {
        return new MensagemResponse(m.getId(), m.getAutor().getNome(), m.getAutor().getPerfil() != Perfil.ASSISTIDO,
                m.isVisivelAssistido(), m.getConteudo(), m.getCriadoEm(), m.getSolicitacao().getStatus());
    }
}

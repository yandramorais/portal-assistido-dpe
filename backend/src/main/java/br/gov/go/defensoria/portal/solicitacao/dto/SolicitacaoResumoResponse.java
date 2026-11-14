package br.gov.go.defensoria.portal.solicitacao.dto;

import java.time.Instant;

import br.gov.go.defensoria.portal.solicitacao.domain.AreaAtuacao;
import br.gov.go.defensoria.portal.solicitacao.domain.Solicitacao;
import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;

/** Linha da lista "Meus pedidos". */
public record SolicitacaoResumoResponse(
        String protocolo,
        String assunto,
        AreaAtuacao area,
        String areaDescricao,
        StatusSolicitacao status,
        String unidade,
        Instant dataAgendamento,
        Instant criadoEm,
        Instant atualizadoEm) {

    public static SolicitacaoResumoResponse de(Solicitacao s) {
        return new SolicitacaoResumoResponse(s.getProtocolo(), s.getAssunto(), s.getArea(),
                s.getArea().getDescricao(), s.getStatus(), s.getUnidade().getNome(), s.getDataAgendamento(),
                s.getCriadoEm(), s.getAtualizadoEm());
    }
}

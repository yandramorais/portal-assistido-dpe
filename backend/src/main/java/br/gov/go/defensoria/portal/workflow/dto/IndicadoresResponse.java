package br.gov.go.defensoria.portal.workflow.dto;

import java.util.List;

import br.gov.go.defensoria.portal.solicitacao.domain.AreaAtuacao;
import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;

/** Totais de pedidos. Todos os status e áreas aparecem, inclusive com zero. */
public record IndicadoresResponse(long total, List<PorStatus> porStatus, List<PorArea> porArea) {

    public record PorStatus(StatusSolicitacao status, String descricao, long total) {
    }

    public record PorArea(AreaAtuacao area, String descricao, long total) {
    }
}

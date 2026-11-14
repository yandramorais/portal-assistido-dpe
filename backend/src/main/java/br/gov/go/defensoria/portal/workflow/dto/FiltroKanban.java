package br.gov.go.defensoria.portal.workflow.dto;

import br.gov.go.defensoria.portal.solicitacao.domain.AreaAtuacao;
import br.gov.go.defensoria.portal.solicitacao.domain.Prioridade;

public record FiltroKanban(AreaAtuacao area, Long unidadeId, Prioridade prioridade, String busca) {
}

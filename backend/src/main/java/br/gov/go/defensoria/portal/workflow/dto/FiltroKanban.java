package br.gov.go.defensoria.portal.workflow.dto;

import br.gov.go.defensoria.portal.solicitacao.domain.AreaAtuacao;
import br.gov.go.defensoria.portal.solicitacao.domain.Prioridade;

/**
 * Filtros opcionais do kanban.
 *
 * @param busca parte do nome/nome social, do CPF (com ou sem máscara) ou do protocolo
 */
public record FiltroKanban(AreaAtuacao area, Long unidadeId, Prioridade prioridade, String busca) {
}

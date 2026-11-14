package br.gov.go.defensoria.portal.workflow.dto;

import java.util.List;

import br.gov.go.defensoria.portal.solicitacao.domain.AreaAtuacao;
import br.gov.go.defensoria.portal.solicitacao.domain.Hipossuficiencia;
import br.gov.go.defensoria.portal.solicitacao.domain.Prioridade;
import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;
import br.gov.go.defensoria.portal.solicitacao.domain.TipoDocumento;

public record KanbanResponse(List<Coluna> colunas) {

    public record Coluna(StatusSolicitacao status, String descricao, int total, List<Card> cards) {
    }

    public record Card(
            String protocolo,
            String nome,
            String cpf,
            AreaAtuacao area,
            String areaDescricao,
            String unidade,
            long diasEmAberto,
            Prioridade prioridade,
            Hipossuficiencia hipossuficiencia,
            List<TipoDocumento> documentosPendentes,
            String responsavel,
            Integer versao) {
    }

    public static String mascararCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return null;
        }
        return "***." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-**";
    }
}

package br.gov.go.defensoria.portal.workflow.dto;

import java.util.List;

import br.gov.go.defensoria.portal.solicitacao.domain.AreaAtuacao;
import br.gov.go.defensoria.portal.solicitacao.domain.Hipossuficiencia;
import br.gov.go.defensoria.portal.solicitacao.domain.Prioridade;
import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;
import br.gov.go.defensoria.portal.solicitacao.domain.TipoDocumento;

/** Quadro da triagem: uma coluna por status, na ordem do fluxo. */
public record KanbanResponse(List<Coluna> colunas) {

    public record Coluna(StatusSolicitacao status, String descricao, int total, List<Card> cards) {
    }

    /**
     * @param nome         nome social, se houver; senão o nome civil
     * @param cpf          mascarado (***.123.456-**)
     * @param diasEmAberto dias corridos desde a abertura (data de Goiânia)
     * @param versao       para o PATCH de status (lock otimista)
     */
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

    /** Mostra só os 6 dígitos do meio, como nas publicações oficiais: ***.123.456-**. */
    public static String mascararCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return null;
        }
        return "***." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-**";
    }
}

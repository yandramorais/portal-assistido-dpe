package br.gov.go.defensoria.portal.workflow.dto;

import java.time.Instant;

import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Mudança de status feita pela equipe. Os campos exigidos dependem do destino:
 * DEFERIDO (dataAgendamento futura + localAtendimento), ENCAMINHADO (destino),
 * INDEFERIDO (motivo), PENDENTE_DOCUMENTACAO (devolutiva).
 *
 * @param versao     versão do pedido que a tela carregou (lock otimista)
 * @param devolutiva mensagem para o assistido; se informada, fica visível no pedido
 */
public record AlterarStatusRequest(
        @NotNull StatusSolicitacao novoStatus,
        @NotNull Integer versao,
        Instant dataAgendamento,
        @Size(max = 200) String localAtendimento,
        @Size(max = 150) String destino,
        @Size(max = 5000) String motivo,
        @Size(max = 5000) String devolutiva) {
}

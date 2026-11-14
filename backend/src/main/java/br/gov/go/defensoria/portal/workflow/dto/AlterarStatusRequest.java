package br.gov.go.defensoria.portal.workflow.dto;

import java.time.Instant;

import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Mudança de status feita pela equipe. Os campos exigidos dependem do destino:
 * DEFERIDO (dataAgendamento futura + localAtendimento), ENCAMINHADO (destino),
 * INDEFERIDO (motivo), PENDENTE_DOCUMENTACAO (devolutiva).
 */
@Schema(description = "Mudança de status. Exemplo: deferir com agendamento.")
public record AlterarStatusRequest(
        @Schema(example = "DEFERIDO") @NotNull StatusSolicitacao novoStatus,
        @Schema(description = "Versão do pedido que a tela carregou (lock otimista)", example = "0")
        @NotNull Integer versao,
        @Schema(description = "Obrigatória e futura para DEFERIDO", example = "2026-11-20T12:00:00Z")
        Instant dataAgendamento,
        @Schema(description = "Obrigatório para DEFERIDO", example = "Sede Goiânia — sala 3")
        @Size(max = 200) String localAtendimento,
        @Schema(description = "Obrigatório para ENCAMINHADO", example = "Procon Goiás")
        @Size(max = 150) String destino,
        @Schema(description = "Obrigatório para INDEFERIDO", example = "Renda acima do limite e sem vulnerabilidade.")
        @Size(max = 5000) String motivo,
        @Schema(description = "Mensagem ao assistido; obrigatória para PENDENTE_DOCUMENTACAO",
                example = "Seu atendimento foi agendado. Leve documento com foto.")
        @Size(max = 5000) String devolutiva) {
}

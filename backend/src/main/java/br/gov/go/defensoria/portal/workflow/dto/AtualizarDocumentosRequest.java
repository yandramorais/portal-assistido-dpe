package br.gov.go.defensoria.portal.workflow.dto;

import java.util.Map;

import br.gov.go.defensoria.portal.solicitacao.domain.TipoDocumento;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Marca documentos do checklist como entregues (true) ou pendentes (false).
 * Só os tipos enviados mudam.
 */
public record AtualizarDocumentosRequest(
        @NotNull Integer versao,
        @NotEmpty Map<TipoDocumento, @NotNull Boolean> documentos) {
}

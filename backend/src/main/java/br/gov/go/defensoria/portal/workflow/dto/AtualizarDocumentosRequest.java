package br.gov.go.defensoria.portal.workflow.dto;

import java.util.Map;

import br.gov.go.defensoria.portal.solicitacao.domain.TipoDocumento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/** Marca documentos do checklist como entregues (true) ou pendentes (false). Só os tipos enviados mudam. */
@Schema(description = "Atualização do checklist de documentos")
public record AtualizarDocumentosRequest(
        @Schema(description = "Versão do pedido que a tela carregou (lock otimista)", example = "0")
        @NotNull Integer versao,
        @Schema(example = "{\"COMPROVANTE_RENDA\": true, \"DOCUMENTOS_DO_CASO\": false}")
        @NotEmpty Map<TipoDocumento, @NotNull Boolean> documentos) {
}

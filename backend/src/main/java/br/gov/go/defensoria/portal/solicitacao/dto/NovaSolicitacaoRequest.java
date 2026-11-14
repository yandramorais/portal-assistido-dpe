package br.gov.go.defensoria.portal.solicitacao.dto;

import java.util.Set;

import br.gov.go.defensoria.portal.solicitacao.domain.AreaAtuacao;
import br.gov.go.defensoria.portal.solicitacao.domain.Modalidade;
import br.gov.go.defensoria.portal.solicitacao.domain.TipoDocumento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Pedido de atendimento feito pelo assistido")
public record NovaSolicitacaoRequest(
        @Schema(description = "Id de uma unidade de GET /api/unidades", example = "1") @NotNull Long unidadeId,
        @Schema(example = "FAMILIA") @NotNull AreaAtuacao area,
        @Schema(example = "Pensão alimentícia") @NotBlank @Size(max = 150) String assunto,
        @Schema(description = "O que aconteceu, com as palavras do assistido",
                example = "O pai dos meus filhos parou de pagar a pensão há três meses.")
        @NotBlank @Size(max = 5000) String relato,
        @Schema(example = "PRESENCIAL") @NotNull Modalidade modalidade,
        @Schema(description = "Documentos que o assistido já tem em mãos; os demais ficam pendentes no checklist",
                example = "[\"DOCUMENTO_IDENTIFICACAO\", \"COMPROVANTE_RESIDENCIA\"]")
        Set<TipoDocumento> documentosEntregues) {
}

package br.gov.go.defensoria.portal.solicitacao.dto;

import java.util.Set;

import br.gov.go.defensoria.portal.solicitacao.domain.AreaAtuacao;
import br.gov.go.defensoria.portal.solicitacao.domain.Modalidade;
import br.gov.go.defensoria.portal.solicitacao.domain.TipoDocumento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @param documentosEntregues documentos que o assistido marcou como já disponíveis
 */
public record NovaSolicitacaoRequest(
        @NotNull Long unidadeId,
        @NotNull AreaAtuacao area,
        @NotBlank @Size(max = 150) String assunto,
        @NotBlank @Size(max = 5000) String relato,
        @NotNull Modalidade modalidade,
        Set<TipoDocumento> documentosEntregues) {
}

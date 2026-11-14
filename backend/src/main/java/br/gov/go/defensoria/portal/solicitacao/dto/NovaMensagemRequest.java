package br.gov.go.defensoria.portal.solicitacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Mensagem no pedido")
public record NovaMensagemRequest(
        @Schema(example = "Enviei o contracheque de outubro.") @NotBlank @Size(max = 5000) String conteudo,
        @Schema(description = "Só a equipe pode mandar false (nota interna). Padrão: true. "
                + "Para o assistido o valor é ignorado e a mensagem é sempre visível.", example = "true")
        Boolean visivelAssistido) {
}

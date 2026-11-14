package br.gov.go.defensoria.portal.solicitacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @param visivelAssistido só a equipe pode mandar false (nota interna). Padrão: true.
 *                         Para o assistido o valor é ignorado e a mensagem é sempre visível.
 */
public record NovaMensagemRequest(
        @NotBlank @Size(max = 5000) String conteudo,
        Boolean visivelAssistido) {
}

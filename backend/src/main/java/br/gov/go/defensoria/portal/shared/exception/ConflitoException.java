package br.gov.go.defensoria.portal.shared.exception;

import lombok.Getter;

/**
 * Operação conflita com um registro existente (HTTP 409).
 * {@code campo} indica qual campo do formulário causou o conflito (ex.: "email", "cpf"),
 * para o front mostrar o erro no lugar certo; pode ser nulo.
 */
@Getter
public class ConflitoException extends RuntimeException {

    private final String campo;

    public ConflitoException(String mensagem) {
        this(null, mensagem);
    }

    public ConflitoException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }
}

package br.gov.go.defensoria.portal.shared.exception;

import lombok.Getter;

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

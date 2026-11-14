package br.gov.go.defensoria.portal.shared.exception;

/** Operação conflita com um registro existente (HTTP 409). */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}

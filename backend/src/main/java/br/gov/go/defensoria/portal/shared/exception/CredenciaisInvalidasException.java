package br.gov.go.defensoria.portal.shared.exception;

/**
 * Login recusado. A mensagem é sempre a mesma para não revelar se o e-mail existe,
 * se a senha está errada ou se o usuário está inativo (HTTP 401).
 */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha inválidos.");
    }
}

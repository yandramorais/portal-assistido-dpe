package br.gov.go.defensoria.portal.shared.exception;

/** Recurso inexistente ou que o usuário não pode ver (HTTP 404). */
public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}

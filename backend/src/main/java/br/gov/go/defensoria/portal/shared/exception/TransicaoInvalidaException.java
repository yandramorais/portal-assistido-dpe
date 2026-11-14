package br.gov.go.defensoria.portal.shared.exception;

import java.util.List;

import lombok.Getter;

/**
 * Mudança de status fora do fluxo permitido, ou sem os campos que o destino exige (HTTP 422).
 * {@code campos} lista os campos que faltam ou estão inválidos, quando for o caso.
 */
@Getter
public class TransicaoInvalidaException extends RuntimeException {

    private final List<String> campos;

    public TransicaoInvalidaException(String mensagem) {
        this(mensagem, List.of());
    }

    public TransicaoInvalidaException(String mensagem, List<String> campos) {
        super(mensagem);
        this.campos = List.copyOf(campos);
    }
}

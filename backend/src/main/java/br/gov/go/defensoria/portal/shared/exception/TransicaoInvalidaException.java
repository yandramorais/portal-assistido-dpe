package br.gov.go.defensoria.portal.shared.exception;

import java.util.List;

import lombok.Getter;

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

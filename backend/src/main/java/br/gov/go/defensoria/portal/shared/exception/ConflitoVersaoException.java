package br.gov.go.defensoria.portal.shared.exception;

public class ConflitoVersaoException extends RuntimeException {

    public static final String MENSAGEM =
            "Este pedido foi atualizado por outra pessoa. Recarregue a página para ver a versão atual.";

    public ConflitoVersaoException() {
        super(MENSAGEM);
    }
}

package br.gov.go.defensoria.portal.shared.exception;

/** O pedido mudou desde que a tela foi carregada (lock otimista, HTTP 409). */
public class ConflitoVersaoException extends RuntimeException {

    public static final String MENSAGEM =
            "Este pedido foi atualizado por outra pessoa. Recarregue a página para ver a versão atual.";

    public ConflitoVersaoException() {
        super(MENSAGEM);
    }
}

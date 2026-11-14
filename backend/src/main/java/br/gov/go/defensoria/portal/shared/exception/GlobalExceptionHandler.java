package br.gov.go.defensoria.portal.shared.exception;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

/** Converte exceções em ProblemDetail (RFC 9457). Erros do Spring MVC vêm da superclasse. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    public record ErroCampo(String campo, String mensagem) {
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ErroCampo> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::erroCampo)
                .toList();

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Verifique os campos informados.");
        problema.setTitle("Dados inválidos");
        problema.setProperty("erros", erros);
        return handleExceptionInternal(ex, problema, headers, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    ProblemDetail credenciaisInvalidas(CredenciaisInvalidasException ex) {
        return problema(HttpStatus.UNAUTHORIZED, "Não autenticado", ex.getMessage());
    }

    @ExceptionHandler(NaoEncontradoException.class)
    ProblemDetail naoEncontrado(NaoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Não encontrado", ex.getMessage());
    }

    @ExceptionHandler(TransicaoInvalidaException.class)
    ProblemDetail transicaoInvalida(TransicaoInvalidaException ex) {
        ProblemDetail problema = problema(HttpStatus.UNPROCESSABLE_ENTITY, "Mudança de status não permitida",
                ex.getMessage());
        if (!ex.getCampos().isEmpty()) {
            problema.setProperty("campos", ex.getCampos());
        }
        return problema;
    }

    /** Versão enviada desatualizada, ou duas gravações simultâneas no mesmo pedido. */
    @ExceptionHandler({ ConflitoVersaoException.class, ObjectOptimisticLockingFailureException.class })
    ProblemDetail conflitoVersao(Exception ex) {
        return problema(HttpStatus.CONFLICT, "Pedido atualizado por outra pessoa", ConflitoVersaoException.MENSAGEM);
    }

    @ExceptionHandler(ConflitoException.class)
    ProblemDetail conflito(ConflitoException ex) {
        return problema(HttpStatus.CONFLICT, "Conflito", ex.getMessage());
    }

    /** Corrida entre duas requisições que passaram pela checagem de duplicidade ao mesmo tempo. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integridade(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade: {}", ex.getMostSpecificCause().getMessage());
        return problema(HttpStatus.CONFLICT, "Conflito", "Os dados informados conflitam com um registro existente.");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail erroInesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Não foi possível concluir a operação. Tente novamente mais tarde.");
    }

    private static ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        return problema;
    }

    private static ErroCampo erroCampo(FieldError erro) {
        return new ErroCampo(erro.getField(), erro.getDefaultMessage());
    }
}

package br.gov.go.defensoria.portal.shared.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import br.gov.go.defensoria.portal.shared.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Converte exceções em ProblemDetail (RFC 9457), sempre em português e sempre com o
 * {@code requestId} da requisição, que o usuário informa ao suporte.
 *
 * <ul>
 * <li>400: validação (lista {@code erros} com campo e mensagem), JSON inválido, parâmetro inválido</li>
 * <li>401, 404, 405, 409 e 422: erros de negócio e de rota</li>
 * <li>500: stack só no log; o cliente recebe mensagem genérica com o requestId</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String PROPRIEDADE_REQUEST_ID = "requestId";

    public record ErroCampo(String campo, String mensagem) {
    }

    // ---------------------------------------------------------------- 400

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ErroCampo> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::erroCampo)
                .toList();

        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Verifique os campos informados.");
        problema.setProperty("erros", erros);
        return handleExceptionInternal(ex, problema, headers, HttpStatus.BAD_REQUEST, request);
    }

    /** JSON mal formado ou valor que não cabe no tipo do campo (ex.: enum inexistente). */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String detalhe = "Corpo da requisição inválido ou mal formatado.";
        if (ex.getCause() instanceof MismatchedInputException mismatch && !mismatch.getPath().isEmpty()) {
            String campo = mismatch.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(nome -> nome != null)
                    .collect(Collectors.joining("."));
            detalhe = "Valor inválido no campo '%s'.".formatted(campo);
        }
        return handleExceptionInternal(ex, problema(HttpStatus.BAD_REQUEST, "Dados inválidos", detalhe), headers,
                HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String parametro = ex instanceof MethodArgumentTypeMismatchException m ? m.getName() : ex.getPropertyName();
        return handleExceptionInternal(ex, problema(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Valor inválido para o parâmetro '%s'.".formatted(parametro)), headers, HttpStatus.BAD_REQUEST,
                request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return handleExceptionInternal(ex, problema(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Informe o parâmetro '%s'.".formatted(ex.getParameterName())), headers, HttpStatus.BAD_REQUEST,
                request);
    }

    // ---------------------------------------------------------------- 401, 404, 405

    @ExceptionHandler(CredenciaisInvalidasException.class)
    ProblemDetail credenciaisInvalidas(CredenciaisInvalidasException ex) {
        return problema(HttpStatus.UNAUTHORIZED, "Não autenticado", ex.getMessage());
    }

    @ExceptionHandler(NaoEncontradoException.class)
    ProblemDetail naoEncontrado(NaoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Não encontrado", ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return handleExceptionInternal(ex, problema(HttpStatus.NOT_FOUND, "Não encontrado",
                "Endereço não encontrado."), headers, HttpStatus.NOT_FOUND, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return handleExceptionInternal(ex, problema(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido",
                "Este endereço não aceita o método %s.".formatted(ex.getMethod())), headers,
                HttpStatus.METHOD_NOT_ALLOWED, request);
    }

    // ---------------------------------------------------------------- 409, 422

    /** Versão enviada desatualizada, ou duas gravações simultâneas no mesmo pedido. */
    @ExceptionHandler({ ConflitoVersaoException.class, ObjectOptimisticLockingFailureException.class })
    ProblemDetail conflitoVersao(Exception ex) {
        return problema(HttpStatus.CONFLICT, "Pedido atualizado por outra pessoa", ConflitoVersaoException.MENSAGEM);
    }

    @ExceptionHandler(ConflitoException.class)
    ProblemDetail conflito(ConflitoException ex) {
        ProblemDetail problema = problema(HttpStatus.CONFLICT, "Conflito", ex.getMessage());
        if (ex.getCampo() != null) {
            problema.setProperty("campo", ex.getCampo());
        }
        return problema;
    }

    /**
     * Corrida entre duas requisições que passaram pela checagem de duplicidade ao mesmo tempo.
     * Loga só o nome da constraint: a mensagem do Postgres traz o valor duplicado (CPF, e-mail).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integridade(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade: constraint={}", nomeDaConstraint(ex));
        return problema(HttpStatus.CONFLICT, "Conflito", "Os dados informados conflitam com um registro existente.");
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

    // ---------------------------------------------------------------- 500

    /** Stack completa no log (com o requestId no padrão); o cliente só recebe o código para o suporte. */
    @ExceptionHandler(Exception.class)
    ProblemDetail erroInesperado(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Não foi possível concluir a operação. Informe ao suporte o código %s."
                        .formatted(RequestIdFilter.atual()));
    }

    // ---------------------------------------------------------------- comum

    /** Garante o requestId também nos ProblemDetail montados pela superclasse. */
    @Override
    protected ResponseEntity<Object> createResponseEntity(Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        if (body instanceof ProblemDetail problema) {
            problema.setProperty(PROPRIEDADE_REQUEST_ID, RequestIdFilter.atual());
        }
        return super.createResponseEntity(body, headers, statusCode, request);
    }

    private static ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        problema.setProperty(PROPRIEDADE_REQUEST_ID, RequestIdFilter.atual());
        return problema;
    }

    private static ErroCampo erroCampo(FieldError erro) {
        return new ErroCampo(erro.getField(), erro.getDefaultMessage());
    }

    private static String nomeDaConstraint(Throwable ex) {
        for (Throwable causa = ex; causa != null; causa = causa.getCause()) {
            if (causa instanceof ConstraintViolationException violacao) {
                return violacao.getConstraintName();
            }
        }
        return "desconhecida";
    }
}

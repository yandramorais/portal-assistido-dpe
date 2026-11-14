package br.gov.go.defensoria.portal.shared.web;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Identificador de correlação de cada requisição. Reaproveita o X-Request-Id recebido (ex.: o
 * $request_id do nginx) se for seguro para log; senão gera um curto. Fica no MDC durante a
 * requisição, volta no header da resposta e entra em todo ProblemDetail, para o usuário
 * informar ao suporte.
 *
 * <p>Roda antes do Spring Security, então 401 e 403 também levam o header.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Request-Id";
    public static final String MDC_REQUEST_ID = "requestId";
    public static final String MDC_USUARIO = "usuario";

    /** Só letras, dígitos, hífen e sublinhado: nada de quebra de linha ou texto forjado no log. */
    private static final Pattern ID_SEGURO = Pattern.compile("[A-Za-z0-9_-]{1,64}");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = idSeguroOuNovo(request.getHeader(HEADER));
        MDC.put(MDC_REQUEST_ID, requestId);
        response.setHeader(HEADER, requestId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_REQUEST_ID);
            MDC.remove(MDC_USUARIO);
        }
    }

    /** requestId da requisição atual (ou da tarefa @Async que herdou o MDC). */
    public static String atual() {
        return MDC.get(MDC_REQUEST_ID);
    }

    static String idSeguroOuNovo(String recebido) {
        if (recebido != null && ID_SEGURO.matcher(recebido).matches()) {
            return recebido;
        }
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /**
     * Coloca no MDC o id do usuário autenticado (sub do JWT), nunca e-mail ou nome. Precisa rodar
     * depois do filtro que valida o token, por isso é registrado no SecurityConfig e não como bean.
     */
    public static class UsuarioNoMdc extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                FilterChain chain) throws ServletException, IOException {
            Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
            if (autenticacao instanceof JwtAuthenticationToken jwt) {
                MDC.put(MDC_USUARIO, jwt.getName());
            }
            try {
                chain.doFilter(request, response);
            } finally {
                MDC.remove(MDC_USUARIO);
            }
        }
    }
}

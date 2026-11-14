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

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Request-Id";
    public static final String MDC_REQUEST_ID = "requestId";
    public static final String MDC_USUARIO = "usuario";

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

    public static String atual() {
        return MDC.get(MDC_REQUEST_ID);
    }

    static String idSeguroOuNovo(String recebido) {
        if (recebido != null && ID_SEGURO.matcher(recebido).matches()) {
            return recebido;
        }
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

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

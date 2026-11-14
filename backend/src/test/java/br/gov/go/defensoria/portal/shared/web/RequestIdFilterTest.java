package br.gov.go.defensoria.portal.shared.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import jakarta.servlet.FilterChain;

class RequestIdFilterTest {

    private static final String ID_GERADO = "[0-9a-f]{12}";

    private final RequestIdFilter filtro = new RequestIdFilter();

    @AfterEach
    void limpar() {
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void semHeaderGeraIdCurtoPoeNoMdcEDevolveNaResposta() throws Exception {
        AtomicReference<String> noMdc = new AtomicReference<>();
        MockHttpServletResponse resposta = new MockHttpServletResponse();

        filtro.doFilter(new MockHttpServletRequest("GET", "/api/unidades"), resposta,
                (req, res) -> noMdc.set(MDC.get(RequestIdFilter.MDC_REQUEST_ID)));

        String devolvido = resposta.getHeader(RequestIdFilter.HEADER);
        assertThat(devolvido).matches(ID_GERADO);
        assertThat(noMdc.get()).isEqualTo(devolvido);
        assertThat(MDC.get(RequestIdFilter.MDC_REQUEST_ID)).as("MDC limpo no fim").isNull();
    }

    @Test
    void reaproveitaIdRecebidoQuandoSeguro() throws Exception {
        MockHttpServletRequest requisicao = new MockHttpServletRequest("GET", "/api/unidades");
        requisicao.addHeader(RequestIdFilter.HEADER, "4f1c9e2a7b3d4c5e_nginx-01");
        MockHttpServletResponse resposta = new MockHttpServletResponse();

        filtro.doFilter(requisicao, resposta, (req, res) -> { });

        assertThat(resposta.getHeader(RequestIdFilter.HEADER)).isEqualTo("4f1c9e2a7b3d4c5e_nginx-01");
    }

    @ParameterizedTest
    @ValueSource(strings = { "abc\nERROR falso no log", "com espaço", "<script>", "",
            "12345678901234567890123456789012345678901234567890123456789012345" })
    void trocaIdInseguroPorUmGerado(String recebido) throws Exception {
        MockHttpServletRequest requisicao = new MockHttpServletRequest("GET", "/api/unidades");
        requisicao.addHeader(RequestIdFilter.HEADER, recebido);
        MockHttpServletResponse resposta = new MockHttpServletResponse();

        filtro.doFilter(requisicao, resposta, (req, res) -> { });

        assertThat(resposta.getHeader(RequestIdFilter.HEADER)).matches(ID_GERADO);
    }

    @Test
    void limpaOMdcMesmoQuandoARequisicaoFalha() {
        FilterChain explode = (req, res) -> {
            throw new IllegalStateException("falhou");
        };

        assertThatThrownBy(() -> filtro.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), explode))
                .isInstanceOf(IllegalStateException.class);
        assertThat(MDC.get(RequestIdFilter.MDC_REQUEST_ID)).isNull();
    }

    @Test
    void usuarioNoMdcUsaSoOIdDoToken() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject("42")
                .claim("nome", "Maria Aparecida da Silva").claim("perfil", "ASSISTIDO").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        AtomicReference<String> noMdc = new AtomicReference<>();

        new RequestIdFilter.UsuarioNoMdc().doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(),
                (req, res) -> noMdc.set(MDC.get(RequestIdFilter.MDC_USUARIO)));

        assertThat(noMdc.get()).isEqualTo("42");
        assertThat(MDC.get(RequestIdFilter.MDC_USUARIO)).isNull();
    }

    @Test
    void anonimoNaoPoeUsuarioNoMdc() throws Exception {
        AtomicReference<String> noMdc = new AtomicReference<>("x");

        new RequestIdFilter.UsuarioNoMdc().doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(),
                (req, res) -> noMdc.set(MDC.get(RequestIdFilter.MDC_USUARIO)));

        assertThat(noMdc.get()).isNull();
    }
}

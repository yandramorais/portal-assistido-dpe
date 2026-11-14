package br.gov.go.defensoria.portal.shared.exception;

import static br.gov.go.defensoria.portal.AutenticacaoDeTeste.MARIA;
import static br.gov.go.defensoria.portal.AutenticacaoDeTeste.SERVIDOR;
import static br.gov.go.defensoria.portal.AutenticacaoDeTeste.bearer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jayway.jsonpath.JsonPath;

import br.gov.go.defensoria.portal.TestcontainersConfiguration;
import br.gov.go.defensoria.portal.shared.web.RequestIdFilter;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, GlobalExceptionHandlerTest.EndpointQueQuebra.class })
@ActiveProfiles("dev")
@Transactional
@ExtendWith(OutputCaptureExtension.class)
class GlobalExceptionHandlerTest {

    private static final String PROBLEM_JSON = "application/problem+json";
    private static final String SEGREDO_INTERNO = "detalhe-interno-que-nao-pode-vazar";

    @RestController
    static class EndpointQueQuebra {
        @GetMapping("/api/teste/erro-inesperado")
        void explodir() {
            throw new IllegalStateException(SEGREDO_INTERNO);
        }
    }

    @Autowired
    private MockMvc mvc;

    @Test
    void validacaoDevolveProblemDetailComListaDeCampos() throws Exception {
        MvcResult resultado = mvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"cpf": "111.111.111-11", "email": "nao-e-email"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON))
                .andExpect(header().exists(RequestIdFilter.HEADER))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Verifique os campos informados."))
                .andExpect(jsonPath("$.instance").value("/api/auth/registro"))
                .andExpect(jsonPath("$.erros[*].campo", containsInAnyOrder("nome", "cpf", "dataNascimento",
                        "telefone", "email", "senha", "cidade", "rendaIndividual")))
                .andExpect(jsonPath("$.erros[?(@.campo == 'cpf')].mensagem", hasItem("CPF inválido")))
                .andExpect(jsonPath("$.erros[?(@.campo == 'nome')].mensagem", hasItem("não deve estar em branco")))
                .andReturn();

        assertThat(requestIdDoCorpo(resultado)).isEqualTo(requestIdDoHeader(resultado));
    }

    @Test
    void jsonMalFormadoDevolve400EmPortugues() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Corpo da requisição inválido ou mal formatado."))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void valorForaDoEnumNoCorpoApontaOCampo() throws Exception {
        mvc.perform(post("/api/solicitacoes")
                .header("Authorization", bearer(mvc, MARIA))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"unidadeId": 1, "area": "ASTRONOMIA", "assunto": "x", "relato": "x", "modalidade": "VIRTUAL"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Valor inválido no campo 'area'."));
    }

    @Test
    void parametroInvalidoNaUrlDevolve400() throws Exception {
        mvc.perform(get("/api/admin/kanban?area=ASTRONOMIA").header("Authorization", bearer(mvc, SERVIDOR)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Valor inválido para o parâmetro 'area'."));
    }

    @Test
    void rotaInexistenteDevolve404EmPortugues() throws Exception {
        mvc.perform(get("/api/nao-existe").header("Authorization", bearer(mvc, SERVIDOR)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Endereço não encontrado."))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void erroInesperadoLogaAStackEDevolveSoOCodigoParaOSuporte(CapturedOutput log) throws Exception {
        MvcResult resultado = mvc.perform(get("/api/teste/erro-inesperado")
                .header("Authorization", bearer(mvc, SERVIDOR)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Erro interno"))
                .andReturn();

        String requestId = requestIdDoHeader(resultado);
        String corpo = resultado.getResponse().getContentAsString();
        assertThat(requestIdDoCorpo(resultado)).isEqualTo(requestId);
        assertThat((String) JsonPath.read(corpo, "$.detail"))
                .isEqualTo("Não foi possível concluir a operação. Informe ao suporte o código " + requestId + ".");
        assertThat(corpo).doesNotContain(SEGREDO_INTERNO).doesNotContain("IllegalStateException");

        assertThat(log.getOut()).contains("ERROR [" + requestId + "]")
                .contains("Erro inesperado em GET /api/teste/erro-inesperado")
                .contains("java.lang.IllegalStateException: " + SEGREDO_INTERNO)
                .contains("at br.gov.go.defensoria.portal.shared.exception.GlobalExceptionHandlerTest");
    }

    @Test
    void naoAutenticadoTambemTrazRequestId() throws Exception {
        MvcResult resultado = mvc.perform(get("/api/solicitacoes/minhas"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(RequestIdFilter.HEADER))
                .andReturn();

        assertThat(requestIdDoCorpo(resultado)).isEqualTo(requestIdDoHeader(resultado));
    }

    @Test
    void requestIdRecebidoVoltaNoHeaderENoCorpo() throws Exception {
        mvc.perform(get("/api/nao-existe")
                .header("Authorization", bearer(mvc, SERVIDOR))
                .header(RequestIdFilter.HEADER, "chamado-4521"))
                .andExpect(header().string(RequestIdFilter.HEADER, "chamado-4521"))
                .andExpect(jsonPath("$.requestId").value("chamado-4521"));
    }

    private static String requestIdDoHeader(MvcResult resultado) {
        return resultado.getResponse().getHeader(RequestIdFilter.HEADER);
    }

    private static String requestIdDoCorpo(MvcResult resultado) throws Exception {
        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.requestId");
    }
}

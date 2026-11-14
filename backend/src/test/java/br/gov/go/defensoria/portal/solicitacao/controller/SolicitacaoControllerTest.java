package br.gov.go.defensoria.portal.solicitacao.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Year;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import br.gov.go.defensoria.portal.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("dev")
@Transactional
class SolicitacaoControllerTest {

    private static final String MARIA = "maria@exemplo.com";
    private static final String JOAO = "joao@exemplo.com";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void assistidoNaoEnxergaPedidoDeOutroAssistido() throws Exception {
        String protocoloDoJoao = protocolosDe("08301661305").getFirst();

        mvc.perform(get("/api/solicitacoes/{p}", protocoloDoJoao).header("Authorization", bearer(JOAO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.protocolo").value(protocoloDoJoao));

        String bearerMaria = bearer(MARIA);
        String deOutro = mvc.perform(get("/api/solicitacoes/{p}", protocoloDoJoao).header("Authorization", bearerMaria))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();
        String inexistente = mvc.perform(get("/api/solicitacoes/{p}", "DPE-2000-999999").header("Authorization", bearerMaria))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        for (String campo : List.of("$.status", "$.title", "$.detail")) {
            assertThat((Object) JsonPath.read(deOutro, campo)).isEqualTo(JsonPath.read(inexistente, campo));
        }
        assertThat((String) JsonPath.read(deOutro, "$.detail")).isEqualTo("Solicitação não encontrada.");

        String minhasMaria = mvc.perform(get("/api/solicitacoes/minhas?size=50").header("Authorization", bearerMaria))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<String> protocolosMaria = JsonPath.read(minhasMaria, "$.content[*].protocolo");
        assertThat(protocolosMaria).containsExactlyInAnyOrderElementsOf(protocolosDe("52601815906"))
                .doesNotContainAnyElementsOf(protocolosDe("08301661305"));
    }

    @Test
    void assistidoNuncaRecebeMensagemInterna() throws Exception {
        String protocolo = jdbc.queryForObject("""
                select s.protocolo from solicitacao s join assistido a on a.id = s.assistido_id
                where a.cpf = '08301661305' and s.area = 'FAMILIA' and s.status = 'PENDENTE_DOCUMENTACAO'
                """, String.class);
        Integer internas = jdbc.queryForObject("""
                select count(*) from mensagem m join solicitacao s on s.id = m.solicitacao_id
                where s.protocolo = ? and not m.visivel_assistido
                """, Integer.class, protocolo);
        assertThat(internas).as("pré-condição do seed").isPositive();

        String corpo = mvc.perform(get("/api/solicitacoes/{p}", protocolo).header("Authorization", bearer(JOAO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagens", hasSize(1)))
                .andExpect(jsonPath("$.mensagens[0].daEquipe").value(true))
                .andReturn().getResponse().getContentAsString();
        assertThat(corpo).doesNotContain("Nota interna");
    }

    @Test
    void criaSolicitacaoComProtocoloRegrasChecklistEHistorico() throws Exception {
        String ano = String.valueOf(Year.now(ZoneId.of("America/Sao_Paulo")).getValue());

        novaSolicitacao(MARIA, """
                ["DOCUMENTO_IDENTIFICACAO", "COMPROVANTE_RESIDENCIA"]""")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/solicitacoes/DPE-" + ano + "-\\d{6}")))
                .andExpect(jsonPath("$.protocolo", matchesPattern("DPE-" + ano + "-\\d{6}")))
                .andExpect(jsonPath("$.status").value("AGUARDANDO_TRIAGEM"))
                .andExpect(jsonPath("$.prioridade").value("ALTA"))
                .andExpect(jsonPath("$.hipossuficiencia").value("PRESUMIDA"))
                .andExpect(jsonPath("$.areaDescricao").value("Saúde"))
                .andExpect(jsonPath("$.documentos", hasSize(5)))
                .andExpect(jsonPath("$.documentos[?(@.tipo == 'DOCUMENTO_IDENTIFICACAO')].entregue").value(true))
                .andExpect(jsonPath("$.documentos[?(@.tipo == 'COMPROVANTE_RESIDENCIA')].entregue").value(true))
                .andExpect(jsonPath("$.documentos[?(@.tipo == 'COMPROVANTE_RENDA')].entregue").value(false))
                .andExpect(jsonPath("$.historico", hasSize(1)))
                .andExpect(jsonPath("$.historico[0].statusAnterior").value(nullValue()))
                .andExpect(jsonPath("$.historico[0].statusNovo").value("AGUARDANDO_TRIAGEM"))
                .andExpect(jsonPath("$.mensagens", hasSize(0)));
    }

    @Test
    void rendaAcimaDoLimiteSemVulnerabilidadeENormalERequerAnalise() throws Exception {
        novaSolicitacao("carlos@exemplo.com", "[]")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.prioridade").value("NORMAL"))
                .andExpect(jsonPath("$.hipossuficiencia").value("REQUER_ANALISE"));
    }

    @Test
    void minhasEPaginadoComMaisRecentesPrimeiro() throws Exception {
        List<String> esperado = jdbc.queryForList("""
                select s.protocolo from solicitacao s join assistido a on a.id = s.assistido_id
                where a.cpf = '52601815906' order by s.criado_em desc, s.id desc
                """, String.class);
        assertThat(esperado).as("pré-condição do seed").hasSizeGreaterThan(1);

        mvc.perform(get("/api/solicitacoes/minhas?size=1&page=0&sort=criadoEm,asc").header("Authorization", bearer(MARIA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].protocolo").value(esperado.getFirst()))
                .andExpect(jsonPath("$.page.totalElements").value(esperado.size()))
                .andExpect(jsonPath("$.page.size").value(1));
    }

    @Test
    void equipeNaoUsaRotasDoAssistido() throws Exception {
        String bearerServidor = bearer("servidor@dpe.local");
        mvc.perform(get("/api/solicitacoes/minhas").header("Authorization", bearerServidor))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/assistidos/me").header("Authorization", bearerServidor))
                .andExpect(status().isForbidden());
    }

    private ResultActions novaSolicitacao(String email, String documentosJson) throws Exception {
        Long unidadeId = jdbc.queryForObject("select id from unidade where comarca = 'Goiânia'", Long.class);
        return mvc.perform(post("/api/solicitacoes")
                .header("Authorization", bearer(email))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "unidadeId": %d,
                          "area": "SAUDE",
                          "assunto": "Fornecimento de medicamento",
                          "relato": "Preciso de medicamento de uso contínuo que está em falta no posto.",
                          "modalidade": "PRESENCIAL",
                          "documentosEntregues": %s
                        }
                        """.formatted(unidadeId, documentosJson)));
    }

    private List<String> protocolosDe(String cpf) {
        return jdbc.queryForList("""
                select s.protocolo from solicitacao s join assistido a on a.id = s.assistido_id
                where a.cpf = ? order by s.id
                """, String.class, cpf);
    }

    private String bearer(String email) throws Exception {
        String corpo = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "senha": "Senha@123"}
                        """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(corpo, "$.token");
    }
}

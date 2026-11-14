package br.gov.go.defensoria.portal.workflow.controller;

import static br.gov.go.defensoria.portal.AutenticacaoDeTeste.MARIA;
import static br.gov.go.defensoria.portal.AutenticacaoDeTeste.SERVIDOR;
import static br.gov.go.defensoria.portal.AutenticacaoDeTeste.bearer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import br.gov.go.defensoria.portal.TestcontainersConfiguration;
import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("dev")
@Transactional
class KanbanControllerTest {

    private static final String CPF_RAFAEL = "93786579741";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;

    private String bearerServidor;

    @BeforeEach
    void setUp() throws Exception {
        bearerServidor = bearer(mvc, SERVIDOR);
    }

    @Test
    void colunasNaOrdemDoFluxoComTotaisDoBanco() throws Exception {
        String kanban = kanban("");

        List<String> status = JsonPath.read(kanban, "$.colunas[*].status");
        assertThat(status).containsExactlyElementsOf(
                Arrays.stream(StatusSolicitacao.values()).map(Enum::name).toList());

        Map<String, Long> doBanco = jdbc.queryForList("select status, count(*) as total from solicitacao group by status")
                .stream().collect(Collectors.toMap(r -> (String) r.get("status"), r -> (Long) r.get("total")));
        List<Integer> totais = JsonPath.read(kanban, "$.colunas[*].total");
        List<List<Object>> cards = JsonPath.read(kanban, "$.colunas[*].cards");
        for (int i = 0; i < status.size(); i++) {
            assertThat(totais.get(i).longValue()).as(status.get(i)).isEqualTo(doBanco.getOrDefault(status.get(i), 0L));
            assertThat(cards.get(i)).hasSize(totais.get(i));
        }
    }

    @Test
    void cardTrazNomeSocialECpfMascarado() throws Exception {
        String kanban = kanban("?busca=rafael");

        List<String> nomes = JsonPath.read(kanban, "$.colunas[*].cards[*].nome");
        List<String> cpfs = JsonPath.read(kanban, "$.colunas[*].cards[*].cpf");
        assertThat(nomes).hasSize(pedidosDoCpf(CPF_RAFAEL)).containsOnly("Rafael Costa");
        assertThat(cpfs).containsOnly("***.865.797-**");
        assertThat(kanban).doesNotContain(CPF_RAFAEL);

        List<Integer> versoes = JsonPath.read(kanban, "$.colunas[*].cards[*].versao");
        assertThat(versoes).doesNotContainNull();
    }

    @Test
    void buscaPorCpfComOuSemMascaraEPorProtocolo() throws Exception {
        List<String> porNome = protocolos(kanban("?busca=rafael"));
        assertThat(protocolos(kanban("?busca=937.865.797-41"))).containsExactlyInAnyOrderElementsOf(porNome);
        assertThat(protocolos(kanban("?busca=" + CPF_RAFAEL))).containsExactlyInAnyOrderElementsOf(porNome);

        String umProtocolo = porNome.getFirst();
        assertThat(protocolos(kanban("?busca=" + umProtocolo.toLowerCase()))).containsExactly(umProtocolo);
    }

    @Test
    void filtrosDeAreaUnidadeEPrioridade() throws Exception {
        String consumidor = kanban("?area=CONSUMIDOR");
        List<String> areas = JsonPath.read(consumidor, "$.colunas[*].cards[*].area");
        assertThat(areas).hasSize(contar("select count(*) from solicitacao where area = 'CONSUMIDOR'"))
                .containsOnly("CONSUMIDOR");

        Long unidadeGoiania = jdbc.queryForObject("select id from unidade where comarca = 'Goiânia'", Long.class);
        String combinado = kanban("?unidadeId=" + unidadeGoiania + "&prioridade=ALTA");
        List<String> prioridades = JsonPath.read(combinado, "$.colunas[*].cards[*].prioridade");
        assertThat(prioridades).hasSize(contar("select count(*) from solicitacao where unidade_id = "
                + unidadeGoiania + " and prioridade = 'ALTA'")).containsOnly("ALTA");
    }

    @Test
    void cardsComPrioridadeAltaPrimeiroEmCadaColuna() throws Exception {
        String kanban = kanban("");
        for (StatusSolicitacao st : StatusSolicitacao.values()) {
            List<String> prioridades = JsonPath.read(kanban,
                    "$.colunas[?(@.status == '" + st + "')].cards[*].prioridade");
            int primeiraNormal = prioridades.indexOf("NORMAL");
            if (primeiraNormal >= 0) {
                assertThat(prioridades.subList(primeiraNormal, prioridades.size())).as(st.name())
                        .containsOnly("NORMAL");
            }
        }
    }

    @Test
    void documentosPendentesDoCard() throws Exception {
        String kanban = kanban("");
        List<List<String>> pendentes = JsonPath.read(kanban,
                "$.colunas[?(@.status == 'AGUARDANDO_TRIAGEM')].cards[*].documentosPendentes");
        assertThat(pendentes).isNotEmpty().allSatisfy(lista -> assertThat(lista)
                .containsExactly("COMPROVANTE_RENDA", "CERTIDAO_CIVIL", "DOCUMENTOS_DO_CASO"));
    }

    @Test
    void indicadoresBatemComOBanco() throws Exception {
        String indicadores = mvc.perform(get("/api/admin/indicadores").header("Authorization", bearerServidor))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int total = contar("select count(*) from solicitacao");
        assertThat((Integer) JsonPath.read(indicadores, "$.total")).isEqualTo(total);
        List<Integer> porStatus = JsonPath.read(indicadores, "$.porStatus[*].total");
        List<Integer> porArea = JsonPath.read(indicadores, "$.porArea[*].total");
        assertThat(porStatus).hasSize(7);
        assertThat(porStatus.stream().mapToInt(Integer::intValue).sum()).isEqualTo(total);
        assertThat(porArea.stream().mapToInt(Integer::intValue).sum()).isEqualTo(total);

        List<Integer> emAnalise = JsonPath.read(indicadores, "$.porStatus[?(@.status == 'EM_ANALISE')].total");
        assertThat(emAnalise).containsExactly(contar("select count(*) from solicitacao where status = 'EM_ANALISE'"));
    }

    @Test
    void notificacoesComFalhaPaginadas() throws Exception {
        String falhas = mvc.perform(get("/api/admin/notificacoes?status=FALHA&size=5")
                .header("Authorization", bearerServidor))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int esperado = contar("select count(*) from notificacao_email where status = 'FALHA'");
        assertThat(esperado).as("pré-condição do seed").isPositive();
        assertThat((Integer) JsonPath.read(falhas, "$.page.totalElements")).isEqualTo(esperado);
        List<String> status = JsonPath.read(falhas, "$.content[*].status");
        List<String> erros = JsonPath.read(falhas, "$.content[*].erro");
        List<String> protocolos = JsonPath.read(falhas, "$.content[*].protocolo");
        assertThat(status).containsOnly("FALHA");
        assertThat(erros).allSatisfy(e -> assertThat(e).contains("550"));
        assertThat(protocolos).doesNotContainNull();
    }

    @Test
    void assistidoNaoAcessaAVisaoDaEquipe() throws Exception {
        String bearerMaria = bearer(mvc, MARIA);
        for (String rota : List.of("/api/admin/kanban", "/api/admin/indicadores", "/api/admin/notificacoes")) {
            mvc.perform(get(rota).header("Authorization", bearerMaria)).andExpect(status().isForbidden());
        }
    }

    private String kanban(String query) throws Exception {
        return mvc.perform(get("/api/admin/kanban" + query).header("Authorization", bearerServidor))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private static List<String> protocolos(String kanban) {
        return JsonPath.read(kanban, "$.colunas[*].cards[*].protocolo");
    }

    private int pedidosDoCpf(String cpf) {
        return contar("select count(*) from solicitacao s join assistido a on a.id = s.assistido_id where a.cpf = '"
                + cpf + "'");
    }

    private int contar(String sql) {
        return jdbc.queryForObject(sql, Integer.class);
    }
}

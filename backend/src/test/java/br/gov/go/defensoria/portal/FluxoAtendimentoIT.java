package br.gov.go.defensoria.portal;

import static br.gov.go.defensoria.portal.AutenticacaoDeTeste.SERVIDOR;
import static br.gov.go.defensoria.portal.AutenticacaoDeTeste.bearer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("dev")
class FluxoAtendimentoIT {

    private static final String CPF = "390.533.447-05";
    private static final String EMAIL = "fluxo.completo@exemplo.com";
    private static final String SENHA = "SenhaForte#1";
    private static final String RELATO = "Meu plano de saúde negou a cirurgia que o médico indicou.";
    private static final String DEVOLUTIVA = "Seu atendimento foi aprovado e agendado. Leve um documento com foto.";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JavaMailSender mailSender;

    @Test
    void doCadastroAoEmailDeDeferimento() throws Exception {
        String bearerAssistido = "Bearer " + JsonPath.read(registrar()
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.token");

        String pedido = mvc.perform(post("/api/solicitacoes")
                .header("Authorization", bearerAssistido)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                        "unidadeId", unidadeDeGoiania(),
                        "area", "SAUDE",
                        "assunto", "Cirurgia negada pelo plano",
                        "relato", RELATO,
                        "modalidade", "PRESENCIAL",
                        "documentosEntregues", List.of("DOCUMENTO_IDENTIFICACAO")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_TRIAGEM"))
                .andReturn().getResponse().getContentAsString();
        String protocolo = JsonPath.read(pedido, "$.protocolo");

        String bearerServidor = bearer(mvc, SERVIDOR);
        mudarStatus(protocolo, bearerServidor, Map.of("novoStatus", "EM_ANALISE", "versao", 0))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"))
                .andExpect(jsonPath("$.versao").value(1));

        Instant agendamento = Instant.now().plus(5, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MINUTES);
        mudarStatus(protocolo, bearerServidor, Map.of(
                "novoStatus", "DEFERIDO",
                "versao", 1,
                "dataAgendamento", agendamento.toString(),
                "localAtendimento", "Sede Goiânia — sala 3",
                "devolutiva", DEVOLUTIVA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEFERIDO"))
                .andExpect(jsonPath("$.versao").value(2));

        mvc.perform(get("/api/solicitacoes/{p}", protocolo).header("Authorization", bearerAssistido))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEFERIDO"))
                .andExpect(jsonPath("$.localAtendimento").value("Sede Goiânia — sala 3"))
                .andExpect(jsonPath("$.dataAgendamento").value(agendamento.toString()))
                .andExpect(jsonPath("$.historico[*].statusNovo",
                        contains("AGUARDANDO_TRIAGEM", "EM_ANALISE", "DEFERIDO")))
                .andExpect(jsonPath("$.mensagens[-1].conteudo").value(DEVOLUTIVA))
                .andExpect(jsonPath("$.mensagens[-1].daEquipe").value(true));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(notificacoesDo(protocolo)).hasSize(2));

        assertThat(notificacoesDo(protocolo)).allSatisfy(notificacao -> {
            assertThat(notificacao.get("status")).isEqualTo("ENVIADO");
            assertThat(notificacao.get("destinatario")).isEqualTo(EMAIL);
            assertThat(notificacao.get("assunto")).isEqualTo("Atualização do seu pedido " + protocolo);
            assertThat(notificacao.get("enviado_em")).isNotNull();
        });

        ArgumentCaptor<SimpleMailMessage> emails = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(emails.capture());
        assertThat(emails.getAllValues()).extracting(SimpleMailMessage::getText)
                .anySatisfy(texto -> assertThat(texto).contains("Situação atual: Em análise"))
                .anySatisfy(texto -> assertThat(texto).contains("Situação atual: Deferido"))
                .allSatisfy(texto -> assertThat(texto)
                        .contains(protocolo)
                        .doesNotContain(RELATO)
                        .doesNotContain(DEVOLUTIVA));
    }

    private ResultActions registrar() throws Exception {
        return mvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                        "nome", "Assistido do Fluxo Completo",
                        "cpf", CPF,
                        "dataNascimento", "1980-07-15",
                        "telefone", "62999998888",
                        "email", EMAIL,
                        "senha", SENHA,
                        "cidade", "Goiânia",
                        "rendaIndividual", 1800.00))));
    }

    private ResultActions mudarStatus(String protocolo, String bearer, Map<String, Object> corpo) throws Exception {
        return mvc.perform(patch("/api/admin/solicitacoes/{p}/status", protocolo)
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(corpo)));
    }

    private List<Map<String, Object>> notificacoesDo(String protocolo) {
        return jdbc.queryForList("""
                select n.status, n.destinatario, n.assunto, n.enviado_em
                from notificacao_email n join solicitacao s on s.id = n.solicitacao_id
                where s.protocolo = ? order by n.id
                """, protocolo);
    }

    private long unidadeDeGoiania() {
        return jdbc.queryForObject("select id from unidade where comarca = 'Goiânia'", Long.class);
    }

    private String json(Map<String, Object> corpo) throws Exception {
        return objectMapper.writeValueAsString(corpo);
    }
}

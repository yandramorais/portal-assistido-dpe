package br.gov.go.defensoria.portal.workflow.controller;

import static br.gov.go.defensoria.portal.AutenticacaoDeTeste.DEFENSOR;
import static br.gov.go.defensoria.portal.AutenticacaoDeTeste.SERVIDOR;
import static br.gov.go.defensoria.portal.AutenticacaoDeTeste.bearer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.gov.go.defensoria.portal.TestcontainersConfiguration;
import br.gov.go.defensoria.portal.notificacao.event.SolicitacaoAtualizadaEvent;
import br.gov.go.defensoria.portal.shared.exception.ConflitoVersaoException;
import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("dev")
@Transactional
@RecordApplicationEvents
class AdminSolicitacaoControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ApplicationEvents eventos;

    @Test
    void pedirDocumentosGravaHistoricoResponsavelDevolutivaEPublicaEvento() throws Exception {
        String protocolo = protocoloEm(StatusSolicitacao.EM_ANALISE);
        int versao = versaoDe(protocolo);

        alterarStatus(protocolo, DEFENSOR, corpo("PENDENTE_DOCUMENTACAO", versao,
                Map.of("devolutiva", "Envie o último contracheque.")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE_DOCUMENTACAO"))
                .andExpect(jsonPath("$.versao").value(versao + 1))
                .andExpect(jsonPath("$.responsavel").value("Dr. Renato Alves"))
                .andExpect(jsonPath("$.historico[-1].statusAnterior").value("EM_ANALISE"))
                .andExpect(jsonPath("$.historico[-1].statusNovo").value("PENDENTE_DOCUMENTACAO"))
                .andExpect(jsonPath("$.historico[-1].observacao").value("Envie o último contracheque."))
                .andExpect(jsonPath("$.historico[-1].autor").value("Dr. Renato Alves"))
                .andExpect(jsonPath("$.mensagens[-1].conteudo").value("Envie o último contracheque."))
                .andExpect(jsonPath("$.mensagens[-1].visivelAssistido").value(true));

        List<SolicitacaoAtualizadaEvent> publicados = eventos.stream(SolicitacaoAtualizadaEvent.class).toList();
        assertThat(publicados).singleElement().satisfies(e -> {
            assertThat(e.protocolo()).isEqualTo(protocolo);
            assertThat(e.statusAnterior()).isEqualTo(StatusSolicitacao.EM_ANALISE);
            assertThat(e.statusNovo()).isEqualTo(StatusSolicitacao.PENDENTE_DOCUMENTACAO);
            assertThat(e.notificarAssistido()).isTrue();
            assertThat(e.destinatario()).contains("@exemplo.com");
        });
    }

    static Stream<Arguments> destinosValidosAPartirDeEmAnalise() {
        String futuro = Instant.now().plus(Duration.ofDays(3)).toString();
        return Stream.of(
                Arguments.of("DEFERIDO", Map.of("dataAgendamento", futuro, "localAtendimento", "Sede Goiânia"),
                        "Atendimento deferido e agendado."),
                Arguments.of("ENCAMINHADO", Map.of("destino", "Procon Goiás"), "Encaminhado para: Procon Goiás"),
                Arguments.of("INDEFERIDO", Map.of("motivo", "Renda acima do limite."), "Renda acima do limite."),
                Arguments.of("PENDENTE_DOCUMENTACAO", Map.of("devolutiva", "Envie o RG."), "Envie o RG."));
    }

    @ParameterizedTest(name = "EM_ANALISE -> {0}")
    @MethodSource("destinosValidosAPartirDeEmAnalise")
    void transicaoValidaComCamposCompletos(String destino, Map<String, Object> campos, String observacao)
            throws Exception {
        String protocolo = protocoloEm(StatusSolicitacao.EM_ANALISE);
        alterarStatus(protocolo, SERVIDOR, corpo(destino, versaoDe(protocolo), campos))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(destino))
                .andExpect(jsonPath("$.historico[-1].observacao").value(observacao));
    }

    static Stream<Arguments> transicoesForaDoFluxo() {
        return Stream.of(
                Arguments.of(StatusSolicitacao.AGUARDANDO_TRIAGEM, "CONCLUIDO"),
                Arguments.of(StatusSolicitacao.AGUARDANDO_TRIAGEM, "ENCAMINHADO"),
                Arguments.of(StatusSolicitacao.DEFERIDO, "INDEFERIDO"),
                Arguments.of(StatusSolicitacao.ENCAMINHADO, "EM_ANALISE"),
                Arguments.of(StatusSolicitacao.CONCLUIDO, "EM_ANALISE"));
    }

    @ParameterizedTest(name = "{0} -> {1} = 422")
    @MethodSource("transicoesForaDoFluxo")
    void transicaoForaDoFluxoDevolve422(StatusSolicitacao origem, String destino) throws Exception {
        String protocolo = protocoloEm(origem);
        Map<String, Object> todosOsCampos = Map.of(
                "dataAgendamento", Instant.now().plus(Duration.ofDays(3)).toString(),
                "localAtendimento", "Sede", "destino", "Procon", "motivo", "Motivo", "devolutiva", "Texto");

        alterarStatus(protocolo, SERVIDOR, corpo(destino, versaoDe(protocolo), todosOsCampos))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Mudança de status não permitida"))
                .andExpect(jsonPath("$.campos").doesNotExist());

        assertThat(statusDe(protocolo)).isEqualTo(origem.name());
        assertThat(eventos.stream(SolicitacaoAtualizadaEvent.class)).isEmpty();
    }

    static Stream<Arguments> camposObrigatoriosPorDestino() {
        String futuro = Instant.now().plus(Duration.ofDays(3)).toString();
        String passado = Instant.now().minus(Duration.ofDays(1)).toString();
        return Stream.of(
                Arguments.of("DEFERIDO", Map.of(), List.of("dataAgendamento", "localAtendimento")),
                Arguments.of("DEFERIDO", Map.of("dataAgendamento", passado, "localAtendimento", "Sede"),
                        List.of("dataAgendamento")),
                Arguments.of("DEFERIDO", Map.of("dataAgendamento", futuro, "localAtendimento", "   "),
                        List.of("localAtendimento")),
                Arguments.of("ENCAMINHADO", Map.of(), List.of("destino")),
                Arguments.of("INDEFERIDO", Map.of("motivo", ""), List.of("motivo")),
                Arguments.of("PENDENTE_DOCUMENTACAO", Map.of(), List.of("devolutiva")));
    }

    @ParameterizedTest(name = "{0} sem {2} = 422")
    @MethodSource("camposObrigatoriosPorDestino")
    void destinoSemCamposObrigatoriosDevolve422(String destino, Map<String, Object> campos, List<String> faltando)
            throws Exception {
        String protocolo = protocoloEm(StatusSolicitacao.EM_ANALISE);

        alterarStatus(protocolo, SERVIDOR, corpo(destino, versaoDe(protocolo), campos))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.campos", contains(faltando.toArray())));

        assertThat(statusDe(protocolo)).isEqualTo("EM_ANALISE");
    }

    @Test
    void versaoDesatualizadaDevolve409() throws Exception {
        String protocolo = protocoloEm(StatusSolicitacao.AGUARDANDO_TRIAGEM);
        int versaoCarregadaPelaTela = versaoDe(protocolo);

        mvc.perform(patch("/api/admin/solicitacoes/{p}/documentos", protocolo)
                .header("Authorization", bearer(mvc, DEFENSOR))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"versao": %d, "documentos": {"COMPROVANTE_RENDA": true}}
                        """.formatted(versaoCarregadaPelaTela)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value(versaoCarregadaPelaTela + 1))
                .andExpect(jsonPath("$.documentos[?(@.tipo == 'COMPROVANTE_RENDA')].entregue").value(true));

        alterarStatus(protocolo, SERVIDOR, corpo("EM_ANALISE", versaoCarregadaPelaTela, Map.of()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(ConflitoVersaoException.MENSAGEM));

        assertThat(statusDe(protocolo)).isEqualTo("AGUARDANDO_TRIAGEM");
    }

    @Test
    void assistidoNaoAcessaRotasDaEquipe() throws Exception {
        String protocolo = protocoloEm(StatusSolicitacao.EM_ANALISE);
        alterarStatus(protocolo, "maria@exemplo.com", corpo("DEFERIDO", 0, Map.of()))
                .andExpect(status().isForbidden());
    }

    private ResultActions alterarStatus(String protocolo, String email, String json) throws Exception {
        return mvc.perform(patch("/api/admin/solicitacoes/{p}/status", protocolo)
                .header("Authorization", bearer(mvc, email))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private String corpo(String novoStatus, int versao, Map<String, Object> campos) throws Exception {
        Map<String, Object> corpo = new HashMap<>(campos);
        corpo.put("novoStatus", novoStatus);
        corpo.put("versao", versao);
        return objectMapper.writeValueAsString(corpo);
    }

    private String protocoloEm(StatusSolicitacao status) {
        return jdbc.queryForObject("select protocolo from solicitacao where status = ? order by id limit 1",
                String.class, status.name());
    }

    private int versaoDe(String protocolo) {
        return jdbc.queryForObject("select versao from solicitacao where protocolo = ?", Integer.class, protocolo);
    }

    private String statusDe(String protocolo) {
        return jdbc.queryForObject("select status from solicitacao where protocolo = ?", String.class, protocolo);
    }
}

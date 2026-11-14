package br.gov.go.defensoria.portal.solicitacao.controller;

import static br.gov.go.defensoria.portal.AutenticacaoDeTeste.JOAO;
import static br.gov.go.defensoria.portal.AutenticacaoDeTeste.MARIA;
import static br.gov.go.defensoria.portal.AutenticacaoDeTeste.SERVIDOR;
import static br.gov.go.defensoria.portal.AutenticacaoDeTeste.bearer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
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

import br.gov.go.defensoria.portal.TestcontainersConfiguration;
import br.gov.go.defensoria.portal.notificacao.event.SolicitacaoAtualizadaEvent;
import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("dev")
@Transactional
@RecordApplicationEvents
class MensagemControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private ApplicationEvents eventos;

    @Test
    void respostaDoAssistidoReabrePedidoPendente() throws Exception {
        String protocolo = pedidoDoJoao("PENDENTE_DOCUMENTACAO");
        String bearerJoao = bearer(mvc, JOAO);

        enviar(protocolo, bearerJoao, "Enviei o contracheque pelo e-mail da unidade.", null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.daEquipe").value(false))
                .andExpect(jsonPath("$.visivelAssistido").value(true))
                .andExpect(jsonPath("$.statusSolicitacao").value("EM_ANALISE"));

        mvc.perform(get("/api/solicitacoes/{p}", protocolo).header("Authorization", bearerJoao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"))
                .andExpect(jsonPath("$.historico[-1].statusAnterior").value("PENDENTE_DOCUMENTACAO"))
                .andExpect(jsonPath("$.historico[-1].statusNovo").value("EM_ANALISE"))
                .andExpect(jsonPath("$.historico[-1].observacao")
                        .value("Assistido respondeu à solicitação de documentos."))
                .andExpect(jsonPath("$.mensagens[-1].conteudo").value("Enviei o contracheque pelo e-mail da unidade."));

        assertThat(eventos.stream(SolicitacaoAtualizadaEvent.class)).singleElement()
                .satisfies(e -> {
                    assertThat(e.statusNovo()).isEqualTo(StatusSolicitacao.EM_ANALISE);
                    assertThat(e.notificarAssistido()).isFalse();
                });
    }

    @Test
    void respostaForaDePendenciaNaoMudaStatus() throws Exception {
        String protocolo = pedidoDoJoao("ENCAMINHADO");

        enviar(protocolo, bearer(mvc, JOAO), "Obrigado pelo encaminhamento.", null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusSolicitacao").value("ENCAMINHADO"));

        assertThat(eventos.stream(SolicitacaoAtualizadaEvent.class)).isEmpty();
    }

    @Test
    void equipeMandaNotaInternaQueOAssistidoNaoVe() throws Exception {
        String protocolo = pedidoDoJoao("PENDENTE_DOCUMENTACAO");
        String nota = "Conferir se já existe ação de guarda na comarca.";

        enviar(protocolo, bearer(mvc, SERVIDOR), nota, false)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.daEquipe").value(true))
                .andExpect(jsonPath("$.visivelAssistido").value(false))
                .andExpect(jsonPath("$.statusSolicitacao").value("PENDENTE_DOCUMENTACAO"));

        String visaoDoAssistido = mvc.perform(get("/api/solicitacoes/{p}", protocolo)
                .header("Authorization", bearer(mvc, JOAO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(visaoDoAssistido).doesNotContain(nota);

        mvc.perform(get("/api/admin/solicitacoes/{p}", protocolo).header("Authorization", bearer(mvc, SERVIDOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagens[-1].conteudo").value(nota))
                .andExpect(jsonPath("$.mensagens[-1].visivelAssistido").value(false));
    }

    @Test
    void assistidoNaoConsegueCriarNotaInterna() throws Exception {
        enviar(pedidoDoJoao("ENCAMINHADO"), bearer(mvc, JOAO), "Tentando esconder.", false)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.visivelAssistido").value(true));
    }

    @Test
    void assistidoNaoEscreveEmPedidoDeOutro() throws Exception {
        String protocolo = pedidoDoJoao("PENDENTE_DOCUMENTACAO");
        int antes = mensagensDe(protocolo);

        enviar(protocolo, bearer(mvc, MARIA), "Mensagem indevida.", null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Solicitação não encontrada."));

        assertThat(mensagensDe(protocolo)).isEqualTo(antes);
        assertThat(jdbc.queryForObject("select status from solicitacao where protocolo = ?", String.class, protocolo))
                .isEqualTo("PENDENTE_DOCUMENTACAO");
    }

    private ResultActions enviar(String protocolo, String bearer, String conteudo, Boolean visivel) throws Exception {
        String visivelJson = visivel == null ? "" : ", \"visivelAssistido\": " + visivel;
        return mvc.perform(post("/api/solicitacoes/{p}/mensagens", protocolo)
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"conteudo\": \"%s\"%s}".formatted(conteudo, visivelJson)));
    }

    private String pedidoDoJoao(String status) {
        return jdbc.queryForObject("""
                select s.protocolo from solicitacao s join assistido a on a.id = s.assistido_id
                where a.cpf = '08301661305' and s.status = ? order by s.id limit 1
                """, String.class, status);
    }

    private int mensagensDe(String protocolo) {
        return jdbc.queryForObject("""
                select count(*) from mensagem m join solicitacao s on s.id = m.solicitacao_id where s.protocolo = ?
                """, Integer.class, protocolo);
    }
}

package br.gov.go.defensoria.portal.shared.web;

import static br.gov.go.defensoria.portal.AutenticacaoDeTeste.SERVIDOR;
import static br.gov.go.defensoria.portal.AutenticacaoDeTeste.bearer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import br.gov.go.defensoria.portal.TestcontainersConfiguration;

/**
 * Percorre o fluxo inteiro (cadastro, login errado, duplicidade, pedido, triagem, mensagem) e
 * confere que os logs de negócio aparecem sem CPF, e-mail, nome ou relato.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("dev")
@Transactional
@ExtendWith(OutputCaptureExtension.class)
class LogSemDadosPessoaisTest {

    private static final String CPF = "529.982.247-25";
    private static final String CPF_DIGITOS = "52998224725";
    private static final String EMAIL = "pessoa.sigilosa@exemplo.com";
    private static final String NOME = "Pessoa Sigilosa de Teste";
    private static final String RELATO = "RELATO-CONFIDENCIAL-DO-CASO";
    private static final String SENHA = "SenhaForte#1";

    @Autowired
    private MockMvc mvc;

    @Test
    void logsDeNegocioSemDadosPessoais(CapturedOutput log) throws Exception {
        registrar().andExpect(status().isCreated());
        login(EMAIL, "senha-errada").andExpect(status().isUnauthorized());
        registrar().andExpect(status().isConflict());

        String bearerAssistido = "Bearer " + JsonPath.read(login(EMAIL, SENHA).andReturn().getResponse()
                .getContentAsString(), "$.token");
        String pedido = mvc.perform(post("/api/solicitacoes")
                .header("Authorization", bearerAssistido)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"unidadeId": 1, "area": "FAMILIA", "assunto": "Guarda", "relato": "%s",
                         "modalidade": "VIRTUAL"}
                        """.formatted(RELATO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String protocolo = JsonPath.read(pedido, "$.protocolo");

        mvc.perform(patch("/api/admin/solicitacoes/{p}/status", protocolo)
                .header("Authorization", bearer(mvc, SERVIDOR))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"novoStatus\": \"EM_ANALISE\", \"versao\": 0}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/solicitacoes/{p}/mensagens", protocolo)
                .header("Authorization", bearerAssistido)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"conteudo\": \"%s\"}".formatted(RELATO)))
                .andExpect(status().isCreated());

        String saida = log.getOut();
        assertThat(saida)
                .contains("Assistido cadastrado: usuarioId=")
                .contains("Login recusado")
                .contains("Cadastro recusado: e-mail já cadastrado")
                .contains("Solicitação criada: protocolo=" + protocolo)
                .contains("Transição de status: protocolo=" + protocolo + " de=AGUARDANDO_TRIAGEM para=EM_ANALISE usuarioId=")
                .contains("Mensagem registrada: protocolo=" + protocolo);
        assertThat(saida)
                .doesNotContain(CPF)
                .doesNotContain(CPF_DIGITOS)
                .doesNotContain(EMAIL)
                .doesNotContain(NOME)
                .doesNotContain(RELATO);
    }

    private ResultActions registrar() throws Exception {
        return mvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome": "%s", "cpf": "%s", "dataNascimento": "1990-01-01", "telefone": "62999990000",
                         "email": "%s", "senha": "%s", "cidade": "Goiânia", "rendaIndividual": 1200.00}
                        """.formatted(NOME, CPF, EMAIL, SENHA)));
    }

    private ResultActions login(String email, String senha) throws Exception {
        return mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"%s\", \"senha\": \"%s\"}".formatted(email, senha)));
    }
}

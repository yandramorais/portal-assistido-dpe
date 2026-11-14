package br.gov.go.defensoria.portal.assistido.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import br.gov.go.defensoria.portal.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("dev")
@Transactional
class AssistidoControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void consultaOsProprioDados() throws Exception {
        mvc.perform(get("/api/assistidos/me").header("Authorization", bearer("juliana@exemplo.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("62819482112"))
                .andExpect(jsonPath("$.nome").value("Juliana Pereira Lima"))
                .andExpect(jsonPath("$.vulnerabilidades[0]").value("VIOLENCIA_DOMESTICA"));
    }

    @Test
    void atualizaOsProprioDadosSemMudarCpf() throws Exception {
        String bearer = bearer("joao@exemplo.com");

        mvc.perform(put("/api/assistidos/me")
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "João Batista Ferreira",
                          "nomeSocial": null,
                          "telefone": "62988887777",
                          "cep": "74000-100",
                          "logradouro": "Rua Nova",
                          "numero": "10",
                          "bairro": "Setor Oeste",
                          "cidade": "Goiânia",
                          "uf": "go",
                          "rendaIndividual": 2500.00,
                          "rendaFamiliar": 2500.00,
                          "membrosFamilia": 4,
                          "possuiImovel": false,
                          "possuiVeiculo": true,
                          "vulnerabilidades": ["DOENCA_GRAVE", "PESSOA_COM_DEFICIENCIA"]
                        }
                        """))
                .andExpect(status().isOk());

        mvc.perform(get("/api/assistidos/me").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("08301661305"))
                .andExpect(jsonPath("$.telefone").value("62988887777"))
                .andExpect(jsonPath("$.cep").value("74000100"))
                .andExpect(jsonPath("$.uf").value("GO"))
                .andExpect(jsonPath("$.vulnerabilidades", containsInAnyOrder("DOENCA_GRAVE", "PESSOA_COM_DEFICIENCIA")));
    }

    @Test
    void atualizacaoInvalidaDevolve400() throws Exception {
        mvc.perform(put("/api/assistidos/me")
                .header("Authorization", bearer("joao@exemplo.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados inválidos"));
    }

    private String bearer(String email) throws Exception {
        String corpo = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "senha": "Senha@123"}
                        """.formatted(email)))
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(corpo, "$.token");
    }
}

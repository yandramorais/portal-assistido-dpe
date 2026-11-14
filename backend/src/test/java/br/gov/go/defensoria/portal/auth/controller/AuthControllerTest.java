package br.gov.go.defensoria.portal.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import br.gov.go.defensoria.portal.TestcontainersConfiguration;
import br.gov.go.defensoria.portal.auth.repository.UsuarioRepository;

/** Fluxo HTTP completo contra o Postgres do Testcontainers com o seed (senha de todos: Senha@123). */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("dev")
@Transactional
class AuthControllerTest {

    private static final String SENHA_SEED = "Senha@123";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JwtDecoder jwtDecoder;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void loginOkDevolveTokenComClaims() throws Exception {
        String token = login("servidor@dpe.local", SENHA_SEED)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.perfil").value("SERVIDOR"))
                .andReturn().getResponse().getContentAsString();

        Jwt jwt = jwtDecoder.decode(JsonPath.read(token, "$.token"));
        Long idServidor = usuarioRepository.findByEmailIgnoreCase("servidor@dpe.local").orElseThrow().getId();

        assertThat(jwt.getSubject()).isEqualTo(idServidor.toString());
        assertThat(jwt.getClaimAsString("perfil")).isEqualTo("SERVIDOR");
        assertThat(jwt.getClaimAsString("nome")).isEqualTo("Carla Mendes (Servidora)");
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofHours(8));
    }

    @Test
    void loginInvalidoTemMensagemGenerica() throws Exception {
        String senhaErrada = login("servidor@dpe.local", "senha-errada")
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos."))
                .andReturn().getResponse().getContentAsString();

        String emailInexistente = login("ninguem@exemplo.com", SENHA_SEED)
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        // Mesmo corpo nos dois casos; só o requestId (um por requisição) muda.
        for (String campo : java.util.List.of("$.type", "$.title", "$.status", "$.detail", "$.instance")) {
            assertThat((Object) JsonPath.read(emailInexistente, campo)).as(campo)
                    .isEqualTo(JsonPath.read(senhaErrada, campo));
        }
    }

    @Test
    void registroCriaAssistidoEJaDevolveToken() throws Exception {
        registro("529.982.247-25", "novo@exemplo.com")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.perfil").value("ASSISTIDO"));

        login("novo@exemplo.com", "SenhaForte#1").andExpect(status().isOk());
    }

    @Test
    void registroComCpfDuplicadoDevolve409() throws Exception {
        // CPF da Maria Aparecida no seed, com máscara.
        registro("526.018.159-06", "outro@exemplo.com")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Já existe cadastro com este CPF."));
    }

    @Test
    void registroComEmailDuplicadoDevolve409() throws Exception {
        registro("529.982.247-25", "MARIA@exemplo.com")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Já existe cadastro com este e-mail."));
    }

    @Test
    void registroComCpfInvalidoDevolve400() throws Exception {
        registro("111.111.111-11", "novo@exemplo.com")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("cpf"))
                .andExpect(jsonPath("$.erros[0].mensagem").value("CPF inválido"));
    }

    @Test
    void rotaAdminNegadaParaAssistido() throws Exception {
        mvc.perform(get("/api/admin/solicitacoes").header("Authorization", bearer("maria@exemplo.com")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Acesso negado"));

        // A equipe passa pela segurança (404 só porque a rota ainda não existe).
        mvc.perform(get("/api/admin/solicitacoes").header("Authorization", bearer("servidor@dpe.local")))
                .andExpect(status().isNotFound());
    }

    @Test
    void rotaProtegidaSemTokenDevolve401() throws Exception {
        mvc.perform(get("/api/solicitacoes"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(jsonPath("$.title").value("Não autenticado"));
    }

    private ResultActions login(String email, String senha) throws Exception {
        return mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "senha": "%s"}
                        """.formatted(email, senha)));
    }

    private ResultActions registro(String cpf, String email) throws Exception {
        return mvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "Novo Assistido",
                          "cpf": "%s",
                          "dataNascimento": "1990-04-10",
                          "telefone": "62999990000",
                          "email": "%s",
                          "senha": "SenhaForte#1",
                          "cidade": "Goiânia",
                          "rendaIndividual": 1500.00,
                          "vulnerabilidades": ["PESSOA_COM_DEFICIENCIA"]
                        }
                        """.formatted(cpf, email)));
    }

    private String bearer(String email) throws Exception {
        String corpo = login(email, SENHA_SEED).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(corpo, "$.token");
    }
}

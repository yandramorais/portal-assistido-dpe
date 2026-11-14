package br.gov.go.defensoria.portal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

public final class AutenticacaoDeTeste {

    public static final String SERVIDOR = "servidor@dpe.local";
    public static final String DEFENSOR = "defensor@dpe.local";
    public static final String MARIA = "maria@exemplo.com";
    public static final String JOAO = "joao@exemplo.com";

    private AutenticacaoDeTeste() {
    }

    public static String bearer(MockMvc mvc, String email) throws Exception {
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

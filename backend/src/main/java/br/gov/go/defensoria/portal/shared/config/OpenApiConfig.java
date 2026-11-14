package br.gov.go.defensoria.portal.shared.config;

import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ProblemDetail;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Portal do Assistido — API",
                version = "v1",
                description = """
                        API do Portal do Assistido da Defensoria Pública do Estado de Goiás.

                        **Autenticação:** faça login em `POST /api/auth/login`, copie o `token` e clique em \
                        *Authorize*. O token vale 8 horas.

                        **Erros:** todas as respostas de erro seguem a RFC 9457 (ProblemDetail) e trazem \
                        `requestId`, o mesmo valor do header `X-Request-Id`. Informe esse código ao suporte."""),
        security = @SecurityRequirement(name = OpenApiConfig.BEARER))
@SecurityScheme(
        name = OpenApiConfig.BEARER,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token devolvido por POST /api/auth/login")
public class OpenApiConfig {

    public static final String BEARER = "bearer";
    private static final String PROBLEM_JSON = "application/problem+json";
    private static final String PROBLEM_DETAIL_REF = "#/components/schemas/ProblemDetail";

    @Bean
    OpenApiCustomizer respostasDeErroPadrao() {
        return openApi -> {
            @SuppressWarnings("rawtypes")
            Map<String, Schema> problemDetail = ModelConverters.getInstance().read(ProblemDetail.class);
            problemDetail.forEach(openApi.getComponents()::addSchemas);

            openApi.getPaths().values().forEach(caminho -> caminho.readOperations().forEach(operacao -> {
                ApiResponses respostas = operacao.getResponses();
                boolean publica = operacao.getSecurity() != null && operacao.getSecurity().isEmpty();
                if (!publica) {
                    respostas.putIfAbsent("401", erro("Não autenticado: token ausente, inválido ou expirado."));
                    respostas.putIfAbsent("403", erro("Seu perfil não tem permissão para esta operação."));
                }
                respostas.putIfAbsent("500", erro("Erro interno. O corpo traz o requestId para informar ao suporte."));

                respostas.forEach((codigo, resposta) -> {
                    if (codigo.matches("[45]\\d\\d") && resposta.getContent() == null) {
                        resposta.setContent(conteudoProblemDetail());
                    }
                });
            }));
        };
    }

    private static ApiResponse erro(String descricao) {
        return new ApiResponse().description(descricao).content(conteudoProblemDetail());
    }

    private static Content conteudoProblemDetail() {
        return new Content().addMediaType(PROBLEM_JSON,
                new MediaType().schema(new Schema<>().$ref(PROBLEM_DETAIL_REF)));
    }
}

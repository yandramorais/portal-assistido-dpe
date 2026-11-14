package br.gov.go.defensoria.portal.notificacao.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.gov.go.defensoria.portal.notificacao.domain.StatusNotificacao;
import br.gov.go.defensoria.portal.notificacao.dto.NotificacaoResponse;
import br.gov.go.defensoria.portal.notificacao.service.NotificacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Equipe: notificações", description = "Log dos e-mails enviados ao assistido, para investigar falhas")
@RestController
@RequestMapping("/api/admin/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @Operation(summary = "Listar e-mails enviados",
            description = "Mais recentes primeiro. Use status=FALHA para ver só os envios com erro.")
    @ApiResponse(responseCode = "200", description = "Página do log de e-mails")
    @ApiResponse(responseCode = "400", description = "Status inválido")
    @GetMapping
    public Page<NotificacaoResponse> listar(
            @Parameter(description = "PENDENTE, ENVIADO ou FALHA", example = "FALHA")
            @RequestParam(required = false) StatusNotificacao status,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return notificacaoService.listar(status, pageable);
    }
}

package br.gov.go.defensoria.portal.notificacao.controller;

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
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Equipe: notificações")
@RestController
@RequestMapping("/api/admin/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @Operation(summary = "Log de e-mails enviados ao assistido, mais recentes primeiro (filtro opcional por status)")
    @GetMapping
    public Page<NotificacaoResponse> listar(@RequestParam(required = false) StatusNotificacao status,
            @PageableDefault(size = 20) Pageable pageable) {
        return notificacaoService.listar(status, pageable);
    }
}

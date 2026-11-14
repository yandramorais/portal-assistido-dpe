package br.gov.go.defensoria.portal.assistido.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.gov.go.defensoria.portal.assistido.dto.AssistidoResponse;
import br.gov.go.defensoria.portal.assistido.dto.AtualizarAssistidoRequest;
import br.gov.go.defensoria.portal.assistido.service.AssistidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Assistido")
@RestController
@RequestMapping("/api/assistidos/me")
@RequiredArgsConstructor
public class AssistidoController {

    private final AssistidoService assistidoService;

    @Operation(summary = "Meus dados")
    @GetMapping
    public AssistidoResponse meusDados(@AuthenticationPrincipal Jwt jwt) {
        return assistidoService.meusDados(Long.valueOf(jwt.getSubject()));
    }

    @Operation(summary = "Atualizar meus dados")
    @PutMapping
    public AssistidoResponse atualizar(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AtualizarAssistidoRequest request) {
        return assistidoService.atualizar(Long.valueOf(jwt.getSubject()), request);
    }
}

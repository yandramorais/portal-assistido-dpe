package br.gov.go.defensoria.portal.workflow.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.gov.go.defensoria.portal.workflow.dto.AlterarStatusRequest;
import br.gov.go.defensoria.portal.workflow.dto.AtualizarDocumentosRequest;
import br.gov.go.defensoria.portal.workflow.dto.SolicitacaoAdminResponse;
import br.gov.go.defensoria.portal.workflow.service.KanbanService;
import br.gov.go.defensoria.portal.workflow.service.TransicaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Equipe: triagem")
@RestController
@RequestMapping("/api/admin/solicitacoes/{protocolo}")
@RequiredArgsConstructor
public class AdminSolicitacaoController {

    private final TransicaoService transicaoService;
    private final KanbanService kanbanService;

    @Operation(summary = "Pedido completo, com notas internas e versão atual")
    @GetMapping
    public SolicitacaoAdminResponse detalhe(@PathVariable String protocolo) {
        return kanbanService.detalhar(protocolo);
    }

    @Operation(summary = "Mudar status (422 se fora do fluxo ou faltando campos; 409 se a versão mudou)")
    @PatchMapping("/status")
    public SolicitacaoAdminResponse alterarStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable String protocolo,
            @Valid @RequestBody AlterarStatusRequest request) {
        return transicaoService.alterarStatus(protocolo, request, Long.valueOf(jwt.getSubject()));
    }

    @Operation(summary = "Marcar documentos do checklist como entregues ou pendentes")
    @PatchMapping("/documentos")
    public SolicitacaoAdminResponse atualizarDocumentos(@PathVariable String protocolo,
            @Valid @RequestBody AtualizarDocumentosRequest request) {
        return kanbanService.atualizarDocumentos(protocolo, request);
    }
}

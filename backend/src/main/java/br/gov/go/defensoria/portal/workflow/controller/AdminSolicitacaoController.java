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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Equipe: triagem", description = "Kanban, mudança de status e checklist (perfis SERVIDOR e DEFENSOR)")
@RestController
@RequestMapping("/api/admin/solicitacoes/{protocolo}")
@RequiredArgsConstructor
public class AdminSolicitacaoController {

    private final TransicaoService transicaoService;
    private final KanbanService kanbanService;

    @Operation(summary = "Ver pedido completo",
            description = "Inclui dados do assistido, notas internas, próximos status possíveis e a versão atual.")
    @ApiResponse(responseCode = "200", description = "Pedido")
    @ApiResponse(responseCode = "404", description = "Protocolo inexistente")
    @GetMapping
    public SolicitacaoAdminResponse detalhe(@Parameter(example = "DPE-2026-000001") @PathVariable String protocolo) {
        return kanbanService.detalhar(protocolo);
    }

    @Operation(summary = "Mudar status",
            description = """
                    Segue o fluxo AGUARDANDO_TRIAGEM → EM_ANALISE → (PENDENTE_DOCUMENTACAO | DEFERIDO | \
                    ENCAMINHADO | INDEFERIDO); PENDENTE_DOCUMENTACAO → EM_ANALISE | INDEFERIDO; \
                    DEFERIDO → CONCLUIDO; INDEFERIDO → EM_ANALISE.

                    Campos exigidos: DEFERIDO (dataAgendamento futura e localAtendimento), ENCAMINHADO (destino), \
                    INDEFERIDO (motivo), PENDENTE_DOCUMENTACAO (devolutiva). Envie a `versao` que a tela carregou. \
                    O assistido é avisado por e-mail.""")
    @ApiResponse(responseCode = "200", description = "Status alterado; a resposta traz a nova versão")
    @ApiResponse(responseCode = "400", description = "Corpo inválido (lista em 'erros')")
    @ApiResponse(responseCode = "404", description = "Protocolo inexistente")
    @ApiResponse(responseCode = "409", description = "O pedido foi alterado por outra pessoa; recarregue")
    @ApiResponse(responseCode = "422", description = "Fora do fluxo, ou faltam campos do destino (lista em 'campos')")
    @PatchMapping("/status")
    public SolicitacaoAdminResponse alterarStatus(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(example = "DPE-2026-000001") @PathVariable String protocolo,
            @Valid @RequestBody AlterarStatusRequest request) {
        return transicaoService.alterarStatus(protocolo, request, Long.valueOf(jwt.getSubject()));
    }

    @Operation(summary = "Marcar documentos do checklist",
            description = "Só os tipos enviados mudam. Envie a `versao` que a tela carregou.")
    @ApiResponse(responseCode = "200", description = "Checklist atualizado; a resposta traz a nova versão")
    @ApiResponse(responseCode = "400", description = "Corpo inválido")
    @ApiResponse(responseCode = "404", description = "Protocolo inexistente")
    @ApiResponse(responseCode = "409", description = "O pedido foi alterado por outra pessoa; recarregue")
    @PatchMapping("/documentos")
    public SolicitacaoAdminResponse atualizarDocumentos(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(example = "DPE-2026-000001") @PathVariable String protocolo,
            @Valid @RequestBody AtualizarDocumentosRequest request) {
        return kanbanService.atualizarDocumentos(protocolo, request, Long.valueOf(jwt.getSubject()));
    }
}

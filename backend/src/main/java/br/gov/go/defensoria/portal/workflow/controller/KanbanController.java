package br.gov.go.defensoria.portal.workflow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.gov.go.defensoria.portal.solicitacao.domain.AreaAtuacao;
import br.gov.go.defensoria.portal.solicitacao.domain.Prioridade;
import br.gov.go.defensoria.portal.workflow.dto.FiltroKanban;
import br.gov.go.defensoria.portal.workflow.dto.IndicadoresResponse;
import br.gov.go.defensoria.portal.workflow.dto.KanbanResponse;
import br.gov.go.defensoria.portal.workflow.service.KanbanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Equipe: triagem")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class KanbanController {

    private final KanbanService kanbanService;

    @Operation(summary = "Quadro de triagem",
            description = "Uma coluna por status, cards com prioridade ALTA primeiro e depois os mais antigos. "
                    + "CPF sai mascarado. Filtros opcionais e combináveis.")
    @ApiResponse(responseCode = "200", description = "Colunas com os cards")
    @ApiResponse(responseCode = "400", description = "Valor de filtro inválido")
    @GetMapping("/kanban")
    public KanbanResponse kanban(
            @Parameter(description = "Área de atuação") @RequestParam(required = false) AreaAtuacao area,
            @Parameter(description = "Id da unidade") @RequestParam(required = false) Long unidadeId,
            @Parameter(description = "Prioridade") @RequestParam(required = false) Prioridade prioridade,
            @Parameter(description = "Parte do nome, do CPF (com ou sem máscara) ou do protocolo",
                    example = "maria") @RequestParam(required = false) String busca) {
        return kanbanService.kanban(new FiltroKanban(area, unidadeId, prioridade, busca));
    }

    @Operation(summary = "Indicadores", description = "Total de pedidos por status e por área, incluindo zeros.")
    @ApiResponse(responseCode = "200", description = "Totais")
    @GetMapping("/indicadores")
    public IndicadoresResponse indicadores() {
        return kanbanService.indicadores();
    }
}

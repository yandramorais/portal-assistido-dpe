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
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Equipe: triagem")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class KanbanController {

    private final KanbanService kanbanService;

    @Operation(summary = "Quadro de triagem com filtros opcionais")
    @GetMapping("/kanban")
    public KanbanResponse kanban(
            @RequestParam(required = false) AreaAtuacao area,
            @RequestParam(required = false) Long unidadeId,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam(required = false) String busca) {
        return kanbanService.kanban(new FiltroKanban(area, unidadeId, prioridade, busca));
    }

    @Operation(summary = "Totais de pedidos por status e por área")
    @GetMapping("/indicadores")
    public IndicadoresResponse indicadores() {
        return kanbanService.indicadores();
    }
}

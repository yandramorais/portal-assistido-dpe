package br.gov.go.defensoria.portal.unidade.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.gov.go.defensoria.portal.unidade.dto.UnidadeResponse;
import br.gov.go.defensoria.portal.unidade.service.UnidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Unidades", description = "Unidades de atendimento da Defensoria")
@SecurityRequirements
@RestController
@RequestMapping("/api/unidades")
@RequiredArgsConstructor
public class UnidadeController {

    private final UnidadeService unidadeService;

    @Operation(summary = "Listar unidades de atendimento", description = "Público. Ordenado por nome.")
    @ApiResponse(responseCode = "200", description = "Lista de unidades")
    @GetMapping
    public List<UnidadeResponse> listar() {
        return unidadeService.listar();
    }
}

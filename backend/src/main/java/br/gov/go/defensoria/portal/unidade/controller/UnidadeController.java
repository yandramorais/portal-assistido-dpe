package br.gov.go.defensoria.portal.unidade.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.gov.go.defensoria.portal.unidade.dto.UnidadeResponse;
import br.gov.go.defensoria.portal.unidade.service.UnidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Unidades")
@RestController
@RequestMapping("/api/unidades")
@RequiredArgsConstructor
public class UnidadeController {

    private final UnidadeService unidadeService;

    @Operation(summary = "Listar unidades de atendimento")
    @GetMapping
    public List<UnidadeResponse> listar() {
        return unidadeService.listar();
    }
}

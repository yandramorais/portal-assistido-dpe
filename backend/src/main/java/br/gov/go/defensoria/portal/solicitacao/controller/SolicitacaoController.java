package br.gov.go.defensoria.portal.solicitacao.controller;

import java.net.URI;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.gov.go.defensoria.portal.solicitacao.dto.MensagemResponse;
import br.gov.go.defensoria.portal.solicitacao.dto.NovaMensagemRequest;
import br.gov.go.defensoria.portal.solicitacao.dto.NovaSolicitacaoRequest;
import br.gov.go.defensoria.portal.solicitacao.dto.SolicitacaoDetalheResponse;
import br.gov.go.defensoria.portal.solicitacao.dto.SolicitacaoResumoResponse;
import br.gov.go.defensoria.portal.solicitacao.service.MensagemService;
import br.gov.go.defensoria.portal.solicitacao.service.SolicitacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Solicitações do assistido",
        description = "Pedidos de atendimento do assistido logado. Pedido de outro assistido responde 404.")
@RestController
@RequestMapping("/api/solicitacoes")
@RequiredArgsConstructor
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;
    private final MensagemService mensagemService;

    @Operation(summary = "Pedir atendimento",
            description = "Gera o protocolo DPE-AAAA-NNNNNN, calcula prioridade (60+ anos ou vulnerabilidade) e "
                    + "hipossuficiência (renda até 3 salários mínimos), e cria o checklist de documentos.")
    @ApiResponse(responseCode = "201", description = "Pedido criado; header Location aponta para ele")
    @ApiResponse(responseCode = "400", description = "Campos inválidos (lista em 'erros')")
    @ApiResponse(responseCode = "404", description = "Unidade não encontrada")
    @PostMapping
    public ResponseEntity<SolicitacaoDetalheResponse> criar(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody NovaSolicitacaoRequest request) {
        SolicitacaoDetalheResponse criada = solicitacaoService.criar(usuarioId(jwt), request);
        return ResponseEntity.created(URI.create("/api/solicitacoes/" + criada.protocolo())).body(criada);
    }

    @Operation(summary = "Listar meus pedidos",
            description = "Paginado (page, size até 50), sempre dos mais recentes para os mais antigos.")
    @ApiResponse(responseCode = "200", description = "Página de pedidos")
    @GetMapping("/minhas")
    public Page<SolicitacaoResumoResponse> minhas(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return solicitacaoService.listarMinhas(usuarioId(jwt), pageable);
    }

    @Operation(summary = "Acompanhar um pedido pelo protocolo",
            description = "Traz checklist, histórico e mensagens visíveis ao assistido (nunca as notas internas).")
    @ApiResponse(responseCode = "200", description = "Detalhe do pedido")
    @ApiResponse(responseCode = "404", description = "Protocolo inexistente ou de outro assistido")
    @GetMapping("/{protocolo}")
    public SolicitacaoDetalheResponse detalhe(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(example = "DPE-2026-000001") @PathVariable String protocolo) {
        return solicitacaoService.buscarMinha(usuarioId(jwt), protocolo);
    }

    @Operation(summary = "Escrever no pedido",
            description = "Assistido: só nos próprios pedidos; se o pedido estiver pendente de documentação, volta "
                    + "para análise. Equipe: em qualquer pedido, e pode mandar nota interna (visivelAssistido=false).")
    @ApiResponse(responseCode = "201", description = "Mensagem registrada")
    @ApiResponse(responseCode = "400", description = "Mensagem vazia ou longa demais")
    @ApiResponse(responseCode = "404", description = "Protocolo inexistente ou, para o assistido, de outra pessoa")
    @PostMapping("/{protocolo}/mensagens")
    @ResponseStatus(HttpStatus.CREATED)
    public MensagemResponse enviarMensagem(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(example = "DPE-2026-000001") @PathVariable String protocolo,
            @Valid @RequestBody NovaMensagemRequest request) {
        return mensagemService.enviar(protocolo, request, usuarioId(jwt));
    }

    private static Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}

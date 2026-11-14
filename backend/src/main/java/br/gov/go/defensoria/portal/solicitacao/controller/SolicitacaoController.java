package br.gov.go.defensoria.portal.solicitacao.controller;

import java.net.URI;

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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Solicitações do assistido")
@RestController
@RequestMapping("/api/solicitacoes")
@RequiredArgsConstructor
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;
    private final MensagemService mensagemService;

    @Operation(summary = "Pedir atendimento")
    @PostMapping
    public ResponseEntity<SolicitacaoDetalheResponse> criar(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody NovaSolicitacaoRequest request) {
        SolicitacaoDetalheResponse criada = solicitacaoService.criar(usuarioId(jwt), request);
        return ResponseEntity.created(URI.create("/api/solicitacoes/" + criada.protocolo())).body(criada);
    }

    @Operation(summary = "Meus pedidos, mais recentes primeiro (parâmetros page e size)")
    @GetMapping("/minhas")
    public Page<SolicitacaoResumoResponse> minhas(@AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 10) Pageable pageable) {
        return solicitacaoService.listarMinhas(usuarioId(jwt), pageable);
    }

    @Operation(summary = "Acompanhar um pedido pelo protocolo")
    @GetMapping("/{protocolo}")
    public SolicitacaoDetalheResponse detalhe(@AuthenticationPrincipal Jwt jwt, @PathVariable String protocolo) {
        return solicitacaoService.buscarMinha(usuarioId(jwt), protocolo);
    }

    @Operation(summary = "Escrever no pedido (assistido: reabre se estiver pendente de documentação; "
            + "equipe: pode mandar nota interna com visivelAssistido=false)")
    @PostMapping("/{protocolo}/mensagens")
    @ResponseStatus(HttpStatus.CREATED)
    public MensagemResponse enviarMensagem(@AuthenticationPrincipal Jwt jwt, @PathVariable String protocolo,
            @Valid @RequestBody NovaMensagemRequest request) {
        return mensagemService.enviar(protocolo, request, usuarioId(jwt));
    }

    private static Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}

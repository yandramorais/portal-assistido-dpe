package br.gov.go.defensoria.portal.solicitacao.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.gov.go.defensoria.portal.auth.domain.Perfil;
import br.gov.go.defensoria.portal.auth.domain.Usuario;
import br.gov.go.defensoria.portal.auth.repository.UsuarioRepository;
import br.gov.go.defensoria.portal.shared.exception.NaoEncontradoException;
import br.gov.go.defensoria.portal.solicitacao.domain.Solicitacao;
import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;
import br.gov.go.defensoria.portal.solicitacao.dto.MensagemResponse;
import br.gov.go.defensoria.portal.solicitacao.dto.NovaMensagemRequest;
import br.gov.go.defensoria.portal.solicitacao.repository.SolicitacaoRepository;
import br.gov.go.defensoria.portal.workflow.domain.Mensagem;
import br.gov.go.defensoria.portal.workflow.repository.MensagemRepository;
import br.gov.go.defensoria.portal.workflow.service.TransicaoService;
import lombok.RequiredArgsConstructor;

/**
 * Conversa do pedido. O assistido só escreve nos próprios pedidos e sempre de forma visível;
 * se o pedido estava pendente de documentação, a resposta dele o devolve para análise.
 * A equipe escreve em qualquer pedido e pode deixar nota interna.
 */
@Service
@RequiredArgsConstructor
public class MensagemService {

    static final String OBSERVACAO_REABERTURA = "Assistido respondeu à solicitação de documentos.";
    private static final String NAO_ENCONTRADA = "Solicitação não encontrada.";

    private final SolicitacaoRepository solicitacaoRepository;
    private final MensagemRepository mensagemRepository;
    private final UsuarioRepository usuarioRepository;
    private final TransicaoService transicaoService;

    @Transactional
    public MensagemResponse enviar(String protocolo, NovaMensagemRequest request, Long usuarioId) {
        Usuario autor = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado."));
        String conteudo = request.conteudo().trim();

        if (autor.getPerfil() == Perfil.ASSISTIDO) {
            // Pedido de outro assistido responde 404, igual a protocolo inexistente.
            Solicitacao solicitacao = solicitacaoRepository.findByProtocoloAndAssistidoUsuarioId(protocolo, usuarioId)
                    .orElseThrow(() -> new NaoEncontradoException(NAO_ENCONTRADA));
            Mensagem mensagem = mensagemRepository.save(new Mensagem(solicitacao, autor, conteudo, true));

            if (solicitacao.getStatus() == StatusSolicitacao.PENDENTE_DOCUMENTACAO) {
                transicaoService.transicionar(solicitacao, StatusSolicitacao.EM_ANALISE, OBSERVACAO_REABERTURA, autor,
                        false);
            }
            return MensagemResponse.de(mensagem);
        }

        Solicitacao solicitacao = solicitacaoRepository.findByProtocolo(protocolo)
                .orElseThrow(() -> new NaoEncontradoException(NAO_ENCONTRADA));
        boolean visivel = request.visivelAssistido() == null || request.visivelAssistido();
        return MensagemResponse.de(mensagemRepository.save(new Mensagem(solicitacao, autor, conteudo, visivel)));
    }
}

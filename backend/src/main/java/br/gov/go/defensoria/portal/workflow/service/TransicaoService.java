package br.gov.go.defensoria.portal.workflow.service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import br.gov.go.defensoria.portal.assistido.domain.Assistido;
import br.gov.go.defensoria.portal.auth.domain.Usuario;
import br.gov.go.defensoria.portal.auth.repository.UsuarioRepository;
import br.gov.go.defensoria.portal.notificacao.event.SolicitacaoAtualizadaEvent;
import br.gov.go.defensoria.portal.shared.exception.ConflitoVersaoException;
import br.gov.go.defensoria.portal.shared.exception.NaoEncontradoException;
import br.gov.go.defensoria.portal.shared.exception.TransicaoInvalidaException;
import br.gov.go.defensoria.portal.solicitacao.domain.Solicitacao;
import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;
import br.gov.go.defensoria.portal.solicitacao.repository.SolicitacaoRepository;
import br.gov.go.defensoria.portal.workflow.domain.HistoricoStatus;
import br.gov.go.defensoria.portal.workflow.domain.Mensagem;
import br.gov.go.defensoria.portal.workflow.dto.AlterarStatusRequest;
import br.gov.go.defensoria.portal.workflow.dto.SolicitacaoAdminResponse;
import br.gov.go.defensoria.portal.workflow.repository.HistoricoStatusRepository;
import br.gov.go.defensoria.portal.workflow.repository.MensagemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Único ponto que muda o status de um pedido: valida o fluxo, grava histórico e publica o evento. */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransicaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final HistoricoStatusRepository historicoRepository;
    private final MensagemRepository mensagemRepository;
    private final UsuarioRepository usuarioRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    /** Mudança feita pela equipe no kanban. */
    @Transactional
    public SolicitacaoAdminResponse alterarStatus(String protocolo, AlterarStatusRequest request, Long usuarioId) {
        Solicitacao solicitacao = solicitacaoRepository.findByProtocolo(protocolo)
                .orElseThrow(() -> new NaoEncontradoException("Solicitação não encontrada."));
        conferirVersao(solicitacao, request.versao());
        conferirFluxo(solicitacao.getStatus(), request.novoStatus());
        conferirCamposDoDestino(request);

        Usuario autor = usuarioRepository.getReferenceById(usuarioId);
        aplicarCamposDoDestino(solicitacao, request);
        solicitacao.setResponsavel(autor);
        transicionar(solicitacao, request.novoStatus(), observacao(request), autor, true);

        if (StringUtils.hasText(request.devolutiva())) {
            mensagemRepository.save(new Mensagem(solicitacao, autor, request.devolutiva().trim(), true));
        }

        // Grava agora para devolver a versão já incrementada.
        solicitacaoRepository.flush();
        return SolicitacaoAdminResponse.de(solicitacao,
                historicoRepository.findBySolicitacaoIdOrderByCriadoEm(solicitacao.getId()),
                mensagemRepository.findBySolicitacaoIdOrderByCriadoEm(solicitacao.getId()));
    }

    /**
     * Aplica a transição, grava o histórico e publica {@link SolicitacaoAtualizadaEvent}.
     * Também usada quando o assistido responde a uma pendência (sem conferir versão).
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void transicionar(Solicitacao solicitacao, StatusSolicitacao destino, String observacao, Usuario autor,
            boolean notificarAssistido) {
        StatusSolicitacao anterior = solicitacao.getStatus();
        conferirFluxo(anterior, destino);

        solicitacao.setStatus(destino);
        historicoRepository.save(new HistoricoStatus(solicitacao, anterior, destino, observacao, autor));
        log.info("Transição de status: protocolo={} de={} para={} usuarioId={}", solicitacao.getProtocolo(),
                anterior, destino, autor == null ? null : autor.getId());
        eventPublisher.publishEvent(new SolicitacaoAtualizadaEvent(solicitacao.getId(), solicitacao.getProtocolo(),
                anterior, destino, emailDoAssistido(solicitacao.getAssistido()), notificarAssistido));
    }

    /** Versão diferente da carregada pela tela: alguém mexeu no pedido nesse meio tempo. */
    public static void conferirVersao(Solicitacao solicitacao, Integer versaoInformada) {
        if (!Objects.equals(solicitacao.getVersao(), versaoInformada)) {
            throw new ConflitoVersaoException();
        }
    }

    private static void conferirFluxo(StatusSolicitacao atual, StatusSolicitacao destino) {
        if (!atual.podeIrPara(destino)) {
            throw new TransicaoInvalidaException("Não é possível passar de \"%s\" para \"%s\"."
                    .formatted(atual.getDescricao(), destino.getDescricao()));
        }
    }

    private void conferirCamposDoDestino(AlterarStatusRequest request) {
        List<String> campos = new ArrayList<>();
        String mensagem = switch (request.novoStatus()) {
            case DEFERIDO -> {
                Instant data = request.dataAgendamento();
                if (data == null || !data.isAfter(Instant.now(clock))) {
                    campos.add("dataAgendamento");
                }
                if (!StringUtils.hasText(request.localAtendimento())) {
                    campos.add("localAtendimento");
                }
                yield "Para deferir, informe a data do atendimento (no futuro) e o local.";
            }
            case ENCAMINHADO -> {
                exigir(request.destino(), "destino", campos);
                yield "Para encaminhar, informe o órgão de destino.";
            }
            case INDEFERIDO -> {
                exigir(request.motivo(), "motivo", campos);
                yield "Para indeferir, informe o motivo.";
            }
            case PENDENTE_DOCUMENTACAO -> {
                exigir(request.devolutiva(), "devolutiva", campos);
                yield "Para pedir documentos, escreva a devolutiva ao assistido.";
            }
            default -> null;
        };
        if (!campos.isEmpty()) {
            throw new TransicaoInvalidaException(mensagem, campos);
        }
    }

    private static void exigir(String valor, String campo, List<String> campos) {
        if (!StringUtils.hasText(valor)) {
            campos.add(campo);
        }
    }

    private static void aplicarCamposDoDestino(Solicitacao solicitacao, AlterarStatusRequest request) {
        switch (request.novoStatus()) {
            case DEFERIDO -> {
                solicitacao.setDataAgendamento(request.dataAgendamento());
                solicitacao.setLocalAtendimento(request.localAtendimento().trim());
            }
            case ENCAMINHADO -> solicitacao.setEncaminhamentoDestino(request.destino().trim());
            case INDEFERIDO -> solicitacao.setMotivoIndeferimento(request.motivo().trim());
            default -> {
                // Demais destinos não têm campos próprios.
            }
        }
    }

    /** Observação do histórico no mesmo padrão do seed. */
    private static String observacao(AlterarStatusRequest request) {
        return switch (request.novoStatus()) {
            case DEFERIDO -> "Atendimento deferido e agendado.";
            case ENCAMINHADO -> "Encaminhado para: " + request.destino().trim();
            case INDEFERIDO -> request.motivo().trim();
            default -> StringUtils.hasText(request.devolutiva()) ? request.devolutiva().trim() : null;
        };
    }

    private static String emailDoAssistido(Assistido assistido) {
        if (StringUtils.hasText(assistido.getEmail())) {
            return assistido.getEmail();
        }
        return assistido.getUsuario() == null ? null : assistido.getUsuario().getEmail();
    }
}

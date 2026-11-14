package br.gov.go.defensoria.portal.solicitacao.service;

import java.time.Clock;
import java.time.Year;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.gov.go.defensoria.portal.assistido.domain.Assistido;
import br.gov.go.defensoria.portal.assistido.service.AssistidoService;
import br.gov.go.defensoria.portal.shared.exception.NaoEncontradoException;
import br.gov.go.defensoria.portal.solicitacao.domain.Solicitacao;
import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;
import br.gov.go.defensoria.portal.solicitacao.domain.TipoDocumento;
import br.gov.go.defensoria.portal.solicitacao.dto.NovaSolicitacaoRequest;
import br.gov.go.defensoria.portal.solicitacao.dto.SolicitacaoDetalheResponse;
import br.gov.go.defensoria.portal.solicitacao.dto.SolicitacaoResumoResponse;
import br.gov.go.defensoria.portal.solicitacao.repository.SolicitacaoRepository;
import br.gov.go.defensoria.portal.unidade.domain.Unidade;
import br.gov.go.defensoria.portal.unidade.repository.UnidadeRepository;
import br.gov.go.defensoria.portal.workflow.domain.HistoricoStatus;
import br.gov.go.defensoria.portal.workflow.repository.HistoricoStatusRepository;
import br.gov.go.defensoria.portal.workflow.repository.MensagemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitacaoService {

    static final int TAMANHO_MAXIMO_PAGINA = 50;
    private static final String NAO_ENCONTRADA = "Solicitação não encontrada.";

    private final SolicitacaoRepository solicitacaoRepository;
    private final UnidadeRepository unidadeRepository;
    private final HistoricoStatusRepository historicoRepository;
    private final MensagemRepository mensagemRepository;
    private final AssistidoService assistidoService;
    private final CalculadoraPrioridade calculadoraPrioridade;
    private final AvaliadorHipossuficiencia avaliadorHipossuficiencia;
    private final Clock clock;

    @Transactional
    public SolicitacaoDetalheResponse criar(Long usuarioId, NovaSolicitacaoRequest request) {
        Assistido assistido = assistidoService.obterPorUsuario(usuarioId);
        Unidade unidade = unidadeRepository.findById(request.unidadeId())
                .orElseThrow(() -> new NaoEncontradoException("Unidade não encontrada."));

        Solicitacao solicitacao = new Solicitacao(gerarProtocolo(), assistido, unidade, request.area(),
                request.assunto().trim(), request.relato().trim(), request.modalidade(),
                calculadoraPrioridade.calcular(assistido.getDataNascimento(), assistido.getVulnerabilidades()),
                avaliadorHipossuficiencia.avaliar(assistido.getRendaIndividual()));

        Set<TipoDocumento> entregues = request.documentosEntregues() == null ? Set.of()
                : request.documentosEntregues();
        for (TipoDocumento tipo : TipoDocumento.values()) {
            solicitacao.getDocumentos().put(tipo, entregues.contains(tipo));
        }
        solicitacaoRepository.save(solicitacao);

        historicoRepository.save(new HistoricoStatus(solicitacao, null, StatusSolicitacao.AGUARDANDO_TRIAGEM,
                "Solicitação registrada pelo assistido.", assistido.getUsuario()));
        log.info("Solicitação criada: protocolo={} area={} prioridade={} hipossuficiencia={} usuarioId={}",
                solicitacao.getProtocolo(), solicitacao.getArea(), solicitacao.getPrioridade(),
                solicitacao.getHipossuficiencia(), usuarioId);

        return detalhe(solicitacao);
    }

    @Transactional(readOnly = true)
    public Page<SolicitacaoResumoResponse> listarMinhas(Long usuarioId, Pageable pageable) {
        Assistido assistido = assistidoService.obterPorUsuario(usuarioId);
        Pageable pagina = PageRequest.of(pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), TAMANHO_MAXIMO_PAGINA),
                Sort.by(Sort.Order.desc("criadoEm"), Sort.Order.desc("id")));
        return solicitacaoRepository.findByAssistidoId(assistido.getId(), pagina)
                .map(SolicitacaoResumoResponse::de);
    }

    @Transactional(readOnly = true)
    public SolicitacaoDetalheResponse buscarMinha(Long usuarioId, String protocolo) {
        Solicitacao solicitacao = solicitacaoRepository.findByProtocoloAndAssistidoUsuarioId(protocolo, usuarioId)
                .orElseThrow(() -> new NaoEncontradoException(NAO_ENCONTRADA));
        return detalhe(solicitacao);
    }

    private SolicitacaoDetalheResponse detalhe(Solicitacao solicitacao) {
        return SolicitacaoDetalheResponse.de(solicitacao,
                historicoRepository.findBySolicitacaoIdOrderByCriadoEm(solicitacao.getId()),
                mensagemRepository.findBySolicitacaoIdAndVisivelAssistidoTrueOrderByCriadoEm(solicitacao.getId()));
    }

    private String gerarProtocolo() {
        return "DPE-%d-%06d".formatted(Year.now(clock).getValue(), solicitacaoRepository.proximoNumeroProtocolo());
    }
}

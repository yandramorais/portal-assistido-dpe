package br.gov.go.defensoria.portal.workflow.service;

import static br.gov.go.defensoria.portal.solicitacao.repository.SolicitacaoSpecifications.comBusca;
import static br.gov.go.defensoria.portal.solicitacao.repository.SolicitacaoSpecifications.comDadosDoCard;
import static br.gov.go.defensoria.portal.solicitacao.repository.SolicitacaoSpecifications.comPrioridade;
import static br.gov.go.defensoria.portal.solicitacao.repository.SolicitacaoSpecifications.daArea;
import static br.gov.go.defensoria.portal.solicitacao.repository.SolicitacaoSpecifications.daUnidade;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.gov.go.defensoria.portal.assistido.domain.Assistido;
import br.gov.go.defensoria.portal.shared.exception.NaoEncontradoException;
import br.gov.go.defensoria.portal.solicitacao.domain.AreaAtuacao;
import br.gov.go.defensoria.portal.solicitacao.domain.Prioridade;
import br.gov.go.defensoria.portal.solicitacao.domain.Solicitacao;
import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;
import br.gov.go.defensoria.portal.solicitacao.domain.TipoDocumento;
import br.gov.go.defensoria.portal.solicitacao.repository.SolicitacaoRepository;
import br.gov.go.defensoria.portal.solicitacao.repository.SolicitacaoRepository.TotalPorArea;
import br.gov.go.defensoria.portal.solicitacao.repository.SolicitacaoRepository.TotalPorStatus;
import br.gov.go.defensoria.portal.workflow.dto.AtualizarDocumentosRequest;
import br.gov.go.defensoria.portal.workflow.dto.FiltroKanban;
import br.gov.go.defensoria.portal.workflow.dto.IndicadoresResponse;
import br.gov.go.defensoria.portal.workflow.dto.KanbanResponse;
import br.gov.go.defensoria.portal.workflow.dto.SolicitacaoAdminResponse;
import br.gov.go.defensoria.portal.workflow.repository.HistoricoStatusRepository;
import br.gov.go.defensoria.portal.workflow.repository.MensagemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Visão da equipe: quadro de triagem, detalhe do pedido, checklist e indicadores. */
@Slf4j
@Service
@RequiredArgsConstructor
public class KanbanService {

    /** ALTA primeiro; dentro da mesma prioridade, os mais antigos primeiro. */
    private static final Comparator<Solicitacao> ORDEM_DO_CARD = Comparator
            .comparing((Solicitacao s) -> s.getPrioridade() == Prioridade.ALTA ? 0 : 1)
            .thenComparing(Solicitacao::getCriadoEm)
            .thenComparing(Solicitacao::getId);

    private final SolicitacaoRepository solicitacaoRepository;
    private final HistoricoStatusRepository historicoRepository;
    private final MensagemRepository mensagemRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public KanbanResponse kanban(FiltroKanban filtro) {
        // Filtros não informados vêm nulos e ficam de fora.
        Specification<Solicitacao> spec = Specification.allOf(Stream.of(
                comDadosDoCard(),
                daArea(filtro.area()),
                daUnidade(filtro.unidadeId()),
                comPrioridade(filtro.prioridade()),
                comBusca(filtro.busca()))
                .filter(Objects::nonNull)
                .toList());

        Map<StatusSolicitacao, List<Solicitacao>> porStatus = new EnumMap<>(StatusSolicitacao.class);
        Arrays.stream(StatusSolicitacao.values()).forEach(st -> porStatus.put(st, new ArrayList<>()));
        solicitacaoRepository.findAll(spec).forEach(s -> porStatus.get(s.getStatus()).add(s));

        LocalDate hoje = LocalDate.now(clock);
        List<KanbanResponse.Coluna> colunas = porStatus.entrySet().stream()
                .map(e -> new KanbanResponse.Coluna(e.getKey(), e.getKey().getDescricao(), e.getValue().size(),
                        e.getValue().stream().sorted(ORDEM_DO_CARD).map(s -> card(s, hoje)).toList()))
                .toList();
        return new KanbanResponse(colunas);
    }

    @Transactional(readOnly = true)
    public SolicitacaoAdminResponse detalhar(String protocolo) {
        return detalhe(buscar(protocolo));
    }

    /** Atualiza só os tipos enviados. Confere a versão como na mudança de status. */
    @Transactional
    public SolicitacaoAdminResponse atualizarDocumentos(String protocolo, AtualizarDocumentosRequest request,
            Long usuarioId) {
        Solicitacao solicitacao = buscar(protocolo);
        TransicaoService.conferirVersao(solicitacao, request.versao());
        solicitacao.getDocumentos().putAll(request.documentos());
        solicitacaoRepository.flush();
        log.info("Checklist de documentos atualizado: protocolo={} documentos={} usuarioId={}", protocolo,
                request.documentos(), usuarioId);
        return detalhe(solicitacao);
    }

    @Transactional(readOnly = true)
    public IndicadoresResponse indicadores() {
        Map<StatusSolicitacao, Long> porStatus = solicitacaoRepository.contarPorStatus().stream()
                .collect(Collectors.toMap(TotalPorStatus::getStatus, TotalPorStatus::getTotal));
        Map<AreaAtuacao, Long> porArea = solicitacaoRepository.contarPorArea().stream()
                .collect(Collectors.toMap(TotalPorArea::getArea, TotalPorArea::getTotal));

        return new IndicadoresResponse(
                porStatus.values().stream().mapToLong(Long::longValue).sum(),
                Arrays.stream(StatusSolicitacao.values())
                        .map(st -> new IndicadoresResponse.PorStatus(st, st.getDescricao(),
                                porStatus.getOrDefault(st, 0L)))
                        .toList(),
                Arrays.stream(AreaAtuacao.values())
                        .map(a -> new IndicadoresResponse.PorArea(a, a.getDescricao(), porArea.getOrDefault(a, 0L)))
                        .toList());
    }

    private Solicitacao buscar(String protocolo) {
        return solicitacaoRepository.findByProtocolo(protocolo)
                .orElseThrow(() -> new NaoEncontradoException("Solicitação não encontrada."));
    }

    private SolicitacaoAdminResponse detalhe(Solicitacao solicitacao) {
        return SolicitacaoAdminResponse.de(solicitacao,
                historicoRepository.findBySolicitacaoIdOrderByCriadoEm(solicitacao.getId()),
                mensagemRepository.findBySolicitacaoIdOrderByCriadoEm(solicitacao.getId()));
    }

    private KanbanResponse.Card card(Solicitacao s, LocalDate hoje) {
        Assistido assistido = s.getAssistido();
        String nome = assistido.getNomeSocial() != null ? assistido.getNomeSocial() : assistido.getNome();
        long dias = ChronoUnit.DAYS.between(s.getCriadoEm().atZone(clock.getZone()).toLocalDate(), hoje);
        List<TipoDocumento> pendentes = Arrays.stream(TipoDocumento.values())
                .filter(tipo -> Boolean.FALSE.equals(s.getDocumentos().get(tipo)))
                .toList();

        return new KanbanResponse.Card(s.getProtocolo(), nome, KanbanResponse.mascararCpf(assistido.getCpf()),
                s.getArea(), s.getArea().getDescricao(), s.getUnidade().getNome(), dias, s.getPrioridade(),
                s.getHipossuficiencia(), pendentes,
                s.getResponsavel() == null ? null : s.getResponsavel().getNome(), s.getVersao());
    }
}

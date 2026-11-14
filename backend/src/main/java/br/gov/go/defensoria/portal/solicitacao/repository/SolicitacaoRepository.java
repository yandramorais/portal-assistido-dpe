package br.gov.go.defensoria.portal.solicitacao.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.gov.go.defensoria.portal.solicitacao.domain.Solicitacao;
import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long>, JpaSpecificationExecutor<Solicitacao> {

    /**
     * Cards do kanban numa única consulta (sem N+1): assistido, unidade e responsável
     * vêm no mesmo SELECT. Prioridade ALTA primeiro, depois as mais antigas.
     */
    @Query("""
            select s from Solicitacao s
              join fetch s.assistido
              join fetch s.unidade
              left join fetch s.responsavel
            where s.status in :status
            order by case when s.prioridade = br.gov.go.defensoria.portal.solicitacao.domain.Prioridade.ALTA
                          then 0 else 1 end,
                     s.criadoEm
            """)
    List<Solicitacao> buscarParaKanban(@Param("status") Collection<StatusSolicitacao> status);

    Optional<Solicitacao> findByProtocolo(String protocolo);

    List<Solicitacao> findByAssistidoIdOrderByCriadoEmDesc(Long assistidoId);

    /** Próximo número da sequência usada no protocolo (DPE-AAAA-NNNNNN). */
    @Query(value = "select nextval('protocolo_seq')", nativeQuery = true)
    long proximoNumeroProtocolo();
}

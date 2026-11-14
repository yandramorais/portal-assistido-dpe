package br.gov.go.defensoria.portal.solicitacao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import br.gov.go.defensoria.portal.solicitacao.domain.AreaAtuacao;
import br.gov.go.defensoria.portal.solicitacao.domain.Solicitacao;
import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long>, JpaSpecificationExecutor<Solicitacao> {

    interface TotalPorStatus {
        StatusSolicitacao getStatus();

        long getTotal();
    }

    interface TotalPorArea {
        AreaAtuacao getArea();

        long getTotal();
    }

    @EntityGraph(attributePaths = { "assistido", "unidade", "responsavel" })
    Optional<Solicitacao> findByProtocolo(String protocolo);

    @EntityGraph(attributePaths = "unidade")
    Page<Solicitacao> findByAssistidoId(Long assistidoId, Pageable pageable);

    @EntityGraph(attributePaths = "unidade")
    Optional<Solicitacao> findByProtocoloAndAssistidoUsuarioId(String protocolo, Long usuarioId);

    @Query("select s.status as status, count(s) as total from Solicitacao s group by s.status")
    List<TotalPorStatus> contarPorStatus();

    @Query("select s.area as area, count(s) as total from Solicitacao s group by s.area")
    List<TotalPorArea> contarPorArea();

    @Query(value = "select nextval('protocolo_seq')", nativeQuery = true)
    long proximoNumeroProtocolo();
}

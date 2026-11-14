package br.gov.go.defensoria.portal.workflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.gov.go.defensoria.portal.workflow.domain.HistoricoStatus;

public interface HistoricoStatusRepository extends JpaRepository<HistoricoStatus, Long> {

    List<HistoricoStatus> findBySolicitacaoIdOrderByCriadoEm(Long solicitacaoId);
}

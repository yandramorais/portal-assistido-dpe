package br.gov.go.defensoria.portal.workflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.gov.go.defensoria.portal.workflow.domain.Mensagem;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    @EntityGraph(attributePaths = "autor")
    List<Mensagem> findBySolicitacaoIdOrderByCriadoEm(Long solicitacaoId);

    @EntityGraph(attributePaths = "autor")
    List<Mensagem> findBySolicitacaoIdAndVisivelAssistidoTrueOrderByCriadoEm(Long solicitacaoId);
}

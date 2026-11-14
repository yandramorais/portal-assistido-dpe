package br.gov.go.defensoria.portal.workflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.gov.go.defensoria.portal.workflow.domain.Mensagem;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    /** Todas, inclusive notas internas: só para a equipe. */
    @EntityGraph(attributePaths = "autor")
    List<Mensagem> findBySolicitacaoIdOrderByCriadoEm(Long solicitacaoId);

    /** Só o que o assistido pode ver (filtrado no SQL), com o autor no mesmo SELECT. */
    @EntityGraph(attributePaths = "autor")
    List<Mensagem> findBySolicitacaoIdAndVisivelAssistidoTrueOrderByCriadoEm(Long solicitacaoId);
}

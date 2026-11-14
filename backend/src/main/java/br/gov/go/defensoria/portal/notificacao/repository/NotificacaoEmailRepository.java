package br.gov.go.defensoria.portal.notificacao.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.gov.go.defensoria.portal.notificacao.domain.NotificacaoEmail;
import br.gov.go.defensoria.portal.notificacao.domain.StatusNotificacao;

public interface NotificacaoEmailRepository extends JpaRepository<NotificacaoEmail, Long> {

    List<NotificacaoEmail> findByStatusOrderByCriadoEm(StatusNotificacao status);

    List<NotificacaoEmail> findBySolicitacaoIdOrderByCriadoEm(Long solicitacaoId);

    /** Solicitação no mesmo SELECT para mostrar o protocolo sem N+1. */
    @EntityGraph(attributePaths = "solicitacao")
    Page<NotificacaoEmail> findByStatus(StatusNotificacao status, Pageable pageable);

    @EntityGraph(attributePaths = "solicitacao")
    Page<NotificacaoEmail> findAllBy(Pageable pageable);
}

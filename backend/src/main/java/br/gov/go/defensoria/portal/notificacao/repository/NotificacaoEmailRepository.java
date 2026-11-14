package br.gov.go.defensoria.portal.notificacao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.gov.go.defensoria.portal.notificacao.domain.NotificacaoEmail;
import br.gov.go.defensoria.portal.notificacao.domain.StatusNotificacao;

public interface NotificacaoEmailRepository extends JpaRepository<NotificacaoEmail, Long> {

    List<NotificacaoEmail> findByStatusOrderByCriadoEm(StatusNotificacao status);

    List<NotificacaoEmail> findBySolicitacaoIdOrderByCriadoEm(Long solicitacaoId);
}

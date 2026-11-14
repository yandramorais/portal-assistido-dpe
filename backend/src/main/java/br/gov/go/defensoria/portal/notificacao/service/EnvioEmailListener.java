package br.gov.go.defensoria.portal.notificacao.service;

import java.time.Clock;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import br.gov.go.defensoria.portal.notificacao.domain.NotificacaoEmail;
import br.gov.go.defensoria.portal.notificacao.domain.StatusNotificacao;
import br.gov.go.defensoria.portal.notificacao.event.SolicitacaoAtualizadaEvent;
import br.gov.go.defensoria.portal.notificacao.repository.NotificacaoEmailRepository;
import br.gov.go.defensoria.portal.solicitacao.repository.SolicitacaoRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Avisa o assistido por e-mail quando o status do pedido muda. Roda só depois do commit
 * (nada de e-mail para mudança desfeita) e fora da thread da requisição. O resultado fica
 * em notificacao_email (ENVIADO ou FALHA com o erro) para a equipe investigar.
 *
 * <p>O e-mail traz só protocolo, status e link: nunca o relato ou dados do caso.
 */
@Slf4j
@Component
public class EnvioEmailListener {

    private final JavaMailSender mailSender;
    private final NotificacaoEmailRepository notificacaoRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final Clock clock;
    private final String frontendUrl;
    private final String remetente;

    public EnvioEmailListener(JavaMailSender mailSender, NotificacaoEmailRepository notificacaoRepository,
            SolicitacaoRepository solicitacaoRepository, Clock clock,
            @Value("${portal.frontend-url}") String frontendUrl,
            @Value("${portal.mail.remetente}") String remetente) {
        this.mailSender = mailSender;
        this.notificacaoRepository = notificacaoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.clock = clock;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
        this.remetente = remetente;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void aoAtualizarSolicitacao(SolicitacaoAtualizadaEvent evento) {
        if (!evento.notificarAssistido()) {
            return;
        }
        if (evento.destinatario() == null) {
            log.warn("Pedido {} sem e-mail do assistido; notificação não enviada.", evento.protocolo());
            return;
        }

        SimpleMailMessage email = montarEmail(evento);
        NotificacaoEmail notificacao = new NotificacaoEmail(
                solicitacaoRepository.getReferenceById(evento.solicitacaoId()), evento.destinatario(),
                email.getSubject());
        try {
            mailSender.send(email);
            notificacao.setStatus(StatusNotificacao.ENVIADO);
            notificacao.setEnviadoEm(Instant.now(clock));
        } catch (RuntimeException e) {
            log.warn("Falha ao enviar e-mail do pedido {}: {}", evento.protocolo(), e.getMessage());
            notificacao.setStatus(StatusNotificacao.FALHA);
            notificacao.setErro(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        notificacaoRepository.save(notificacao);
    }

    SimpleMailMessage montarEmail(SolicitacaoAtualizadaEvent evento) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(remetente);
        email.setTo(evento.destinatario());
        email.setSubject("Atualização do seu pedido " + evento.protocolo());
        email.setText("""
                Olá,

                Seu pedido %s na Defensoria Pública do Estado de Goiás foi atualizado.

                Situação atual: %s

                Para ver os detalhes, acesse o Portal do Assistido:
                %s/meus-pedidos/%s

                Esta é uma mensagem automática. Não responda este e-mail.
                """.formatted(evento.protocolo(), evento.statusNovo().getDescricao(), frontendUrl,
                evento.protocolo()));
        return email;
    }
}

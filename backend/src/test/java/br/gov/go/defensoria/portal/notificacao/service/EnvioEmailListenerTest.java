package br.gov.go.defensoria.portal.notificacao.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import br.gov.go.defensoria.portal.notificacao.domain.NotificacaoEmail;
import br.gov.go.defensoria.portal.notificacao.domain.StatusNotificacao;
import br.gov.go.defensoria.portal.notificacao.event.SolicitacaoAtualizadaEvent;
import br.gov.go.defensoria.portal.notificacao.repository.NotificacaoEmailRepository;
import br.gov.go.defensoria.portal.solicitacao.domain.Solicitacao;
import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;
import br.gov.go.defensoria.portal.solicitacao.repository.SolicitacaoRepository;

@ExtendWith(MockitoExtension.class)
class EnvioEmailListenerTest {

    private static final Instant AGORA = Instant.parse("2026-11-14T15:00:00Z");
    private static final String PROTOCOLO = "DPE-2026-000007";

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private NotificacaoEmailRepository notificacaoRepository;
    @Mock
    private SolicitacaoRepository solicitacaoRepository;

    private EnvioEmailListener listener;

    @BeforeEach
    void setUp() {
        listener = new EnvioEmailListener(mailSender, notificacaoRepository, solicitacaoRepository,
                Clock.fixed(AGORA, ZoneId.of("America/Sao_Paulo")), "http://localhost:5173/", "portal@dpe.local");
    }

    @Test
    void envioComSucessoGravaEnviado() {
        when(solicitacaoRepository.getReferenceById(7L)).thenReturn(mock(Solicitacao.class));

        listener.aoAtualizarSolicitacao(evento("maria@exemplo.com", true));

        ArgumentCaptor<SimpleMailMessage> email = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(email.capture());
        assertThat(email.getValue().getTo()).containsExactly("maria@exemplo.com");
        assertThat(email.getValue().getFrom()).isEqualTo("portal@dpe.local");
        assertThat(email.getValue().getSubject()).isEqualTo("Atualização do seu pedido " + PROTOCOLO);
        assertThat(email.getValue().getText())
                .contains(PROTOCOLO)
                .contains("Situação atual: Pendente de documentação")
                .contains("http://localhost:5173/meus-pedidos/" + PROTOCOLO);

        NotificacaoEmail gravada = notificacaoGravada();
        assertThat(gravada.getStatus()).isEqualTo(StatusNotificacao.ENVIADO);
        assertThat(gravada.getEnviadoEm()).isEqualTo(AGORA);
        assertThat(gravada.getErro()).isNull();
        assertThat(gravada.getDestinatario()).isEqualTo("maria@exemplo.com");
        assertThat(gravada.getAssunto()).isEqualTo("Atualização do seu pedido " + PROTOCOLO);
    }

    @Test
    void falhaNoEnvioGravaFalhaComOErro() {
        when(solicitacaoRepository.getReferenceById(7L)).thenReturn(mock(Solicitacao.class));
        doThrow(new MailSendException("550 5.1.1 mailbox unavailable"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        listener.aoAtualizarSolicitacao(evento("inexistente@exemplo.com", true));

        NotificacaoEmail gravada = notificacaoGravada();
        assertThat(gravada.getStatus()).isEqualTo(StatusNotificacao.FALHA);
        assertThat(gravada.getErro()).isEqualTo("MailSendException: 550 5.1.1 mailbox unavailable");
        assertThat(gravada.getEnviadoEm()).isNull();
    }

    @Test
    void naoAvisaQuandoAMudancaPartiuDoAssistido() {
        listener.aoAtualizarSolicitacao(evento("maria@exemplo.com", false));
        verifyNoInteractions(mailSender, notificacaoRepository);
    }

    @Test
    void semEmailDoAssistidoNaoTentaEnviar() {
        listener.aoAtualizarSolicitacao(evento(null, true));
        verifyNoInteractions(mailSender, notificacaoRepository);
    }

    @Test
    void rodaDepoisDoCommitForaDaThreadDaRequisicaoEEmTransacaoPropria() throws Exception {
        var metodo = EnvioEmailListener.class.getMethod("aoAtualizarSolicitacao", SolicitacaoAtualizadaEvent.class);
        assertThat(metodo.getAnnotation(TransactionalEventListener.class).phase())
                .isEqualTo(TransactionPhase.AFTER_COMMIT);
        assertThat(metodo.getAnnotation(Async.class)).isNotNull();
        assertThat(metodo.getAnnotation(Transactional.class).propagation()).isEqualTo(Propagation.REQUIRES_NEW);
    }

    private static SolicitacaoAtualizadaEvent evento(String destinatario, boolean notificar) {
        return new SolicitacaoAtualizadaEvent(7L, PROTOCOLO, StatusSolicitacao.EM_ANALISE,
                StatusSolicitacao.PENDENTE_DOCUMENTACAO, destinatario, notificar);
    }

    private NotificacaoEmail notificacaoGravada() {
        ArgumentCaptor<NotificacaoEmail> captor = ArgumentCaptor.forClass(NotificacaoEmail.class);
        verify(notificacaoRepository).save(captor.capture());
        return captor.getValue();
    }
}

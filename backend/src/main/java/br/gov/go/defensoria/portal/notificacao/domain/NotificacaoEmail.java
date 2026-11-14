package br.gov.go.defensoria.portal.notificacao.domain;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import br.gov.go.defensoria.portal.solicitacao.domain.Solicitacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notificacao_email")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificacaoEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitacao_id", nullable = false)
    private Solicitacao solicitacao;

    @Column(nullable = false, length = 150)
    private String destinatario;

    @Column(nullable = false, length = 200)
    private String assunto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusNotificacao status = StatusNotificacao.PENDENTE;

    @Column(columnDefinition = "text")
    private String erro;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant criadoEm;

    @Column(name = "enviado_em")
    private Instant enviadoEm;

    public NotificacaoEmail(Solicitacao solicitacao, String destinatario, String assunto) {
        this.solicitacao = solicitacao;
        this.destinatario = destinatario;
        this.assunto = assunto;
    }
}

package br.gov.go.defensoria.portal.workflow.domain;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import br.gov.go.defensoria.portal.auth.domain.Usuario;
import br.gov.go.defensoria.portal.solicitacao.domain.Solicitacao;
import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;
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
@Table(name = "historico_status")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistoricoStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitacao_id", nullable = false)
    private Solicitacao solicitacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 30)
    private StatusSolicitacao statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false, length = 30)
    private StatusSolicitacao statusNovo;

    @Column(columnDefinition = "text")
    private String observacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant criadoEm;

    public HistoricoStatus(Solicitacao solicitacao, StatusSolicitacao statusAnterior, StatusSolicitacao statusNovo,
            String observacao, Usuario autor) {
        this.solicitacao = solicitacao;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.observacao = observacao;
        this.autor = autor;
    }
}

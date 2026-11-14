package br.gov.go.defensoria.portal.workflow.domain;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import br.gov.go.defensoria.portal.auth.domain.Usuario;
import br.gov.go.defensoria.portal.solicitacao.domain.Solicitacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "mensagem")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitacao_id", nullable = false)
    private Solicitacao solicitacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(nullable = false, columnDefinition = "text")
    private String conteudo;

    @Column(name = "visivel_assistido", nullable = false)
    private boolean visivelAssistido = true;

    @Column(name = "lida_em")
    private Instant lidaEm;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant criadoEm;

    public Mensagem(Solicitacao solicitacao, Usuario autor, String conteudo, boolean visivelAssistido) {
        this.solicitacao = solicitacao;
        this.autor = autor;
        this.conteudo = conteudo;
        this.visivelAssistido = visivelAssistido;
    }
}

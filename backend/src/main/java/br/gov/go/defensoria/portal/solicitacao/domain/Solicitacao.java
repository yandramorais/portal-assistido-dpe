package br.gov.go.defensoria.portal.solicitacao.domain;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import br.gov.go.defensoria.portal.assistido.domain.Assistido;
import br.gov.go.defensoria.portal.auth.domain.Usuario;
import br.gov.go.defensoria.portal.unidade.domain.Unidade;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "solicitacao")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Solicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, length = 20, unique = true, updatable = false)
    private String protocolo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assistido_id", nullable = false)
    private Assistido assistido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidade_id", nullable = false)
    private Unidade unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AreaAtuacao area;

    @Column(nullable = false, length = 150)
    private String assunto;

    @Column(nullable = false, columnDefinition = "text")
    private String relato;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Modalidade modalidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusSolicitacao status = StatusSolicitacao.AGUARDANDO_TRIAGEM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Prioridade prioridade = Prioridade.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Hipossuficiencia hipossuficiencia;

    @Column(name = "data_agendamento")
    private Instant dataAgendamento;

    @Column(name = "local_atendimento", length = 200)
    private String localAtendimento;

    @Column(name = "encaminhamento_destino", length = 150)
    private String encaminhamentoDestino;

    @Column(name = "motivo_indeferimento", columnDefinition = "text")
    private String motivoIndeferimento;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "solicitacao_documento", joinColumns = @JoinColumn(name = "solicitacao_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "tipo", length = 40)
    @Column(name = "entregue", nullable = false)
    private Map<TipoDocumento, Boolean> documentos = new HashMap<>();

    @Version
    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private Integer versao;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    @Setter(AccessLevel.NONE)
    private Instant atualizadoEm;

    public Solicitacao(String protocolo, Assistido assistido, Unidade unidade, AreaAtuacao area, String assunto,
            String relato, Modalidade modalidade, Prioridade prioridade, Hipossuficiencia hipossuficiencia) {
        this.protocolo = protocolo;
        this.assistido = assistido;
        this.unidade = unidade;
        this.area = area;
        this.assunto = assunto;
        this.relato = relato;
        this.modalidade = modalidade;
        this.prioridade = prioridade;
        this.hipossuficiencia = hipossuficiencia;
    }
}

package br.gov.go.defensoria.portal.assistido.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import br.gov.go.defensoria.portal.auth.domain.Usuario;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "assistido")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Assistido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "nome_social", length = 150)
    private String nomeSocial;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(length = 150)
    private String email;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 8)
    private String cep;

    @Column(length = 150)
    private String logradouro;

    @Column(length = 10)
    private String numero;

    @Column(length = 80)
    private String complemento;

    @Column(length = 80)
    private String bairro;

    @Column(nullable = false, length = 80)
    private String cidade;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 2)
    private String uf = "GO";

    @Column(name = "renda_individual", nullable = false, precision = 12, scale = 2)
    private BigDecimal rendaIndividual;

    @Column(name = "renda_familiar", precision = 12, scale = 2)
    private BigDecimal rendaFamiliar;

    @Column(name = "membros_familia", nullable = false)
    private Short membrosFamilia = 1;

    @Column(name = "possui_imovel", nullable = false)
    private boolean possuiImovel;

    @Column(name = "possui_veiculo", nullable = false)
    private boolean possuiVeiculo;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "assistido_vulnerabilidade", joinColumns = @JoinColumn(name = "assistido_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "vulnerabilidade", nullable = false, length = 40)
    private Set<Vulnerabilidade> vulnerabilidades = new HashSet<>();

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    @Setter(AccessLevel.NONE)
    private Instant atualizadoEm;

    public Assistido(String nome, String cpf, LocalDate dataNascimento, String telefone, String cidade,
            BigDecimal rendaIndividual) {
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.cidade = cidade;
        this.rendaIndividual = rendaIndividual;
    }
}

package br.gov.go.defensoria.portal.unidade.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "unidade")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Unidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 80)
    private String comarca;

    @Column(length = 200)
    private String endereco;

    @Column(length = 20)
    private String telefone;

    public Unidade(String nome, String comarca) {
        this.nome = nome;
        this.comarca = comarca;
    }
}

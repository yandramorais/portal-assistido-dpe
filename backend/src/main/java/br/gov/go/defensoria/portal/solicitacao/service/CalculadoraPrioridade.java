package br.gov.go.defensoria.portal.solicitacao.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.Set;

import org.springframework.stereotype.Component;

import br.gov.go.defensoria.portal.assistido.domain.Vulnerabilidade;
import br.gov.go.defensoria.portal.solicitacao.domain.Prioridade;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CalculadoraPrioridade {

    static final int IDADE_PESSOA_IDOSA = 60;

    private final Clock clock;

    public Prioridade calcular(LocalDate dataNascimento, Set<Vulnerabilidade> vulnerabilidades) {
        boolean pessoaIdosa = Period.between(dataNascimento, LocalDate.now(clock)).getYears() >= IDADE_PESSOA_IDOSA;
        boolean vulneravel = vulnerabilidades != null && !vulnerabilidades.isEmpty();
        return pessoaIdosa || vulneravel ? Prioridade.ALTA : Prioridade.NORMAL;
    }
}

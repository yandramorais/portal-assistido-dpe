package br.gov.go.defensoria.portal.solicitacao.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import br.gov.go.defensoria.portal.shared.config.DpeProperties;
import br.gov.go.defensoria.portal.solicitacao.domain.Hipossuficiencia;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AvaliadorHipossuficiencia {

    private final DpeProperties dpe;

    public Hipossuficiencia avaliar(BigDecimal rendaIndividual) {
        return rendaIndividual.compareTo(dpe.limiteRendaPresumida()) <= 0
                ? Hipossuficiencia.PRESUMIDA
                : Hipossuficiencia.REQUER_ANALISE;
    }
}

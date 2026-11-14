package br.gov.go.defensoria.portal.solicitacao.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AreaAtuacao {
    FAMILIA("Família"),
    CIVEL("Cível"),
    CONSUMIDOR("Consumidor"),
    SAUDE("Saúde"),
    MORADIA("Moradia"),
    REGISTRO_CIVIL("Registro civil"),
    INFANCIA_JUVENTUDE("Infância e juventude"),
    DEFESA_MULHER("Defesa da mulher"),
    IDOSO_PCD("Pessoa idosa e pessoa com deficiência");

    private final String descricao;
}

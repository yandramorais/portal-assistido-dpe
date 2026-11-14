package br.gov.go.defensoria.portal.unidade.dto;

import br.gov.go.defensoria.portal.unidade.domain.Unidade;

public record UnidadeResponse(Long id, String nome, String comarca, String endereco, String telefone) {

    public static UnidadeResponse de(Unidade unidade) {
        return new UnidadeResponse(unidade.getId(), unidade.getNome(), unidade.getComarca(), unidade.getEndereco(),
                unidade.getTelefone());
    }
}

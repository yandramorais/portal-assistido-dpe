package br.gov.go.defensoria.portal.assistido.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

import br.gov.go.defensoria.portal.assistido.domain.Assistido;
import br.gov.go.defensoria.portal.assistido.domain.Vulnerabilidade;

public record AssistidoResponse(
        Long id,
        String nome,
        String nomeSocial,
        String cpf,
        LocalDate dataNascimento,
        String telefone,
        String email,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        BigDecimal rendaIndividual,
        BigDecimal rendaFamiliar,
        Short membrosFamilia,
        boolean possuiImovel,
        boolean possuiVeiculo,
        Set<Vulnerabilidade> vulnerabilidades) {

    public static AssistidoResponse de(Assistido a) {
        Set<Vulnerabilidade> vulnerabilidades = a.getVulnerabilidades().isEmpty()
                ? EnumSet.noneOf(Vulnerabilidade.class)
                : EnumSet.copyOf(a.getVulnerabilidades());
        return new AssistidoResponse(a.getId(), a.getNome(), a.getNomeSocial(), a.getCpf(), a.getDataNascimento(),
                a.getTelefone(), a.getEmail(), a.getCep(), a.getLogradouro(), a.getNumero(), a.getComplemento(),
                a.getBairro(), a.getCidade(), a.getUf(), a.getRendaIndividual(), a.getRendaFamiliar(),
                a.getMembrosFamilia(), a.isPossuiImovel(), a.isPossuiVeiculo(), vulnerabilidades);
    }
}

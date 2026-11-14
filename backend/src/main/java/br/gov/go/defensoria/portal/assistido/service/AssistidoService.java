package br.gov.go.defensoria.portal.assistido.service;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import br.gov.go.defensoria.portal.assistido.domain.Assistido;
import br.gov.go.defensoria.portal.assistido.dto.AssistidoResponse;
import br.gov.go.defensoria.portal.assistido.dto.AtualizarAssistidoRequest;
import br.gov.go.defensoria.portal.assistido.repository.AssistidoRepository;
import br.gov.go.defensoria.portal.shared.exception.NaoEncontradoException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssistidoService {

    private final AssistidoRepository assistidoRepository;

    /** Ficha do assistido ligada ao usuário logado. Deve ser chamado dentro de uma transação. */
    public Assistido obterPorUsuario(Long usuarioId) {
        return assistidoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new NaoEncontradoException("Cadastro de assistido não encontrado."));
    }

    @Transactional(readOnly = true)
    public AssistidoResponse meusDados(Long usuarioId) {
        return AssistidoResponse.de(obterPorUsuario(usuarioId));
    }

    @Transactional
    public AssistidoResponse atualizar(Long usuarioId, AtualizarAssistidoRequest request) {
        Assistido assistido = obterPorUsuario(usuarioId);
        String nomeSocial = StringUtils.hasText(request.nomeSocial()) ? request.nomeSocial().trim() : null;

        assistido.setNome(request.nome().trim());
        assistido.setNomeSocial(nomeSocial);
        assistido.setTelefone(request.telefone().trim());
        assistido.setCep(request.cep() == null ? null : request.cep().replace("-", ""));
        assistido.setLogradouro(request.logradouro());
        assistido.setNumero(request.numero());
        assistido.setComplemento(request.complemento());
        assistido.setBairro(request.bairro());
        assistido.setCidade(request.cidade().trim());
        assistido.setUf(request.uf().toUpperCase(Locale.ROOT));
        assistido.setRendaIndividual(request.rendaIndividual());
        assistido.setRendaFamiliar(request.rendaFamiliar());
        assistido.setMembrosFamilia(request.membrosFamilia());
        assistido.setPossuiImovel(request.possuiImovel());
        assistido.setPossuiVeiculo(request.possuiVeiculo());
        assistido.getVulnerabilidades().clear();
        assistido.getVulnerabilidades().addAll(request.vulnerabilidades());

        // Nome exibido no sistema (token, telas da equipe) respeita o nome social.
        assistido.getUsuario().setNome(nomeSocial != null ? nomeSocial : assistido.getNome());

        return AssistidoResponse.de(assistido);
    }
}

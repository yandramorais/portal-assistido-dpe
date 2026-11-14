package br.gov.go.defensoria.portal.unidade.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.gov.go.defensoria.portal.unidade.dto.UnidadeResponse;
import br.gov.go.defensoria.portal.unidade.repository.UnidadeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UnidadeService {

    private final UnidadeRepository unidadeRepository;

    @Transactional(readOnly = true)
    public List<UnidadeResponse> listar() {
        return unidadeRepository.findAllByOrderByNome().stream().map(UnidadeResponse::de).toList();
    }
}

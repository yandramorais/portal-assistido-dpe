package br.gov.go.defensoria.portal.notificacao.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.gov.go.defensoria.portal.notificacao.domain.StatusNotificacao;
import br.gov.go.defensoria.portal.notificacao.dto.NotificacaoResponse;
import br.gov.go.defensoria.portal.notificacao.repository.NotificacaoEmailRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    static final int TAMANHO_MAXIMO_PAGINA = 100;

    private final NotificacaoEmailRepository notificacaoRepository;

    @Transactional(readOnly = true)
    public Page<NotificacaoResponse> listar(StatusNotificacao status, Pageable pageable) {
        Pageable pagina = PageRequest.of(pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), TAMANHO_MAXIMO_PAGINA),
                Sort.by(Sort.Order.desc("criadoEm"), Sort.Order.desc("id")));
        return (status == null
                ? notificacaoRepository.findAllBy(pagina)
                : notificacaoRepository.findByStatus(status, pagina))
                .map(NotificacaoResponse::de);
    }
}

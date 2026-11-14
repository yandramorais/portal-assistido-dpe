package br.gov.go.defensoria.portal.assistido.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.gov.go.defensoria.portal.assistido.domain.Assistido;

public interface AssistidoRepository extends JpaRepository<Assistido, Long> {

    Optional<Assistido> findByCpf(String cpf);

    Optional<Assistido> findByUsuarioId(Long usuarioId);

    boolean existsByCpf(String cpf);
}

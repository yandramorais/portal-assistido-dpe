package br.gov.go.defensoria.portal.unidade.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.gov.go.defensoria.portal.unidade.domain.Unidade;

public interface UnidadeRepository extends JpaRepository<Unidade, Long> {

    List<Unidade> findAllByOrderByNome();
}

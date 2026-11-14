package br.gov.go.defensoria.portal.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.gov.go.defensoria.portal.auth.domain.Perfil;
import br.gov.go.defensoria.portal.auth.domain.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<Usuario> findByPerfilInAndAtivoTrueOrderByNome(List<Perfil> perfis);
}

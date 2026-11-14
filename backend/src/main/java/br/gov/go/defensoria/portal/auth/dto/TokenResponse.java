package br.gov.go.defensoria.portal.auth.dto;

import java.time.Instant;

import br.gov.go.defensoria.portal.auth.domain.Perfil;

public record TokenResponse(String token, String tipo, Instant expiraEm, UsuarioLogado usuario) {

    public record UsuarioLogado(Long id, String nome, Perfil perfil) {
    }
}

package br.gov.go.defensoria.portal.auth.service;

import java.util.Locale;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import br.gov.go.defensoria.portal.assistido.domain.Assistido;
import br.gov.go.defensoria.portal.assistido.repository.AssistidoRepository;
import br.gov.go.defensoria.portal.auth.domain.Perfil;
import br.gov.go.defensoria.portal.auth.domain.Usuario;
import br.gov.go.defensoria.portal.auth.dto.LoginRequest;
import br.gov.go.defensoria.portal.auth.dto.RegistroRequest;
import br.gov.go.defensoria.portal.auth.dto.TokenResponse;
import br.gov.go.defensoria.portal.auth.repository.UsuarioRepository;
import br.gov.go.defensoria.portal.shared.exception.ConflitoException;
import br.gov.go.defensoria.portal.shared.exception.CredenciaisInvalidasException;
import br.gov.go.defensoria.portal.shared.validation.CpfValidator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final AssistidoRepository assistidoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final String hashFicticio;

    public AuthService(UsuarioRepository usuarioRepository, AssistidoRepository assistidoRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.assistidoRepository = assistidoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.hashFicticio = passwordEncoder.encode("senha-ficticia-para-tempo-constante");
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        Optional<Usuario> usuario = usuarioRepository.findByEmailIgnoreCase(request.email().trim());

        boolean senhaConfere = passwordEncoder.matches(request.senha(),
                usuario.map(Usuario::getSenhaHash).orElse(hashFicticio));

        if (usuario.isEmpty() || !senhaConfere || !usuario.get().isAtivo()) {
            log.info("Login recusado");
            throw new CredenciaisInvalidasException();
        }
        log.info("Login: usuarioId={} perfil={}", usuario.get().getId(), usuario.get().getPerfil());
        return emitirToken(usuario.get());
    }

    @Transactional
    public TokenResponse registrar(RegistroRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String cpf = CpfValidator.somenteDigitos(request.cpf());

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            log.info("Cadastro recusado: e-mail já cadastrado");
            throw new ConflitoException("email", "Já existe cadastro com este e-mail.");
        }
        if (assistidoRepository.existsByCpf(cpf)) {
            log.info("Cadastro recusado: CPF já cadastrado");
            throw new ConflitoException("cpf", "Já existe cadastro com este CPF.");
        }

        String nomeExibicao = StringUtils.hasText(request.nomeSocial()) ? request.nomeSocial().trim()
                : request.nome().trim();
        Usuario usuario = usuarioRepository.save(
                new Usuario(nomeExibicao, email, passwordEncoder.encode(request.senha()), Perfil.ASSISTIDO));

        Assistido assistido = new Assistido(request.nome().trim(), cpf, request.dataNascimento(),
                request.telefone().trim(), request.cidade().trim(), request.rendaIndividual());
        assistido.setUsuario(usuario);
        assistido.setEmail(email);
        assistido.setNomeSocial(StringUtils.hasText(request.nomeSocial()) ? request.nomeSocial().trim() : null);
        assistido.setCep(request.cep() == null ? null : request.cep().replace("-", ""));
        assistido.setLogradouro(request.logradouro());
        assistido.setNumero(request.numero());
        assistido.setComplemento(request.complemento());
        assistido.setBairro(request.bairro());
        if (request.uf() != null) {
            assistido.setUf(request.uf().toUpperCase(Locale.ROOT));
        }
        assistido.setRendaFamiliar(request.rendaFamiliar());
        if (request.membrosFamilia() != null) {
            assistido.setMembrosFamilia(request.membrosFamilia());
        }
        assistido.setPossuiImovel(Boolean.TRUE.equals(request.possuiImovel()));
        assistido.setPossuiVeiculo(Boolean.TRUE.equals(request.possuiVeiculo()));
        if (request.vulnerabilidades() != null) {
            assistido.getVulnerabilidades().addAll(request.vulnerabilidades());
        }
        assistidoRepository.save(assistido);
        log.info("Assistido cadastrado: usuarioId={}", usuario.getId());

        return emitirToken(usuario);
    }

    private TokenResponse emitirToken(Usuario usuario) {
        JwtService.TokenEmitido token = jwtService.gerar(usuario);
        return new TokenResponse(token.valor(), "Bearer", token.expiraEm(),
                new TokenResponse.UsuarioLogado(usuario.getId(), usuario.getNome(), usuario.getPerfil()));
    }
}

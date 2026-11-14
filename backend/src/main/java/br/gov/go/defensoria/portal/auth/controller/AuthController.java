package br.gov.go.defensoria.portal.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.gov.go.defensoria.portal.auth.dto.LoginRequest;
import br.gov.go.defensoria.portal.auth.dto.RegistroRequest;
import br.gov.go.defensoria.portal.auth.dto.TokenResponse;
import br.gov.go.defensoria.portal.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Autenticação", description = "Login da equipe e do assistido, e cadastro do assistido")
@SecurityRequirements
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Entrar com e-mail e senha",
            description = "Devolve um token JWT (Bearer) válido por 8 horas, com o id, o nome e o perfil do usuário. "
                    + "A resposta de erro é a mesma para e-mail inexistente e senha errada.")
    @ApiResponse(responseCode = "200", description = "Login feito")
    @ApiResponse(responseCode = "400", description = "E-mail ou senha não informados")
    @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos")
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Cadastrar assistido",
            description = "Cria o login (perfil ASSISTIDO) e a ficha socioeconômica na mesma operação e já "
                    + "devolve o token. O CPF é validado pelos dígitos verificadores.")
    @ApiResponse(responseCode = "201", description = "Cadastro criado; o assistido já está logado")
    @ApiResponse(responseCode = "400", description = "Campos inválidos (lista em 'erros')")
    @ApiResponse(responseCode = "409", description = "E-mail ou CPF já cadastrado")
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse registrar(@Valid @RequestBody RegistroRequest request) {
        return authService.registrar(request);
    }
}

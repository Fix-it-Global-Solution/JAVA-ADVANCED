package com.fixit.api.controller;

import com.fixit.api.dto.LoginRequestDTO;
import com.fixit.api.dto.TokenResponseDTO;
import com.fixit.api.dto.UsuarioRequestDTO;
import com.fixit.api.dto.UsuarioResponseDTO;
import com.fixit.api.model.Usuario;
import com.fixit.api.security.TokenService;
import com.fixit.api.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "1. Autenticação e Registro", description = "Endpoints públicos para login JWT e cadastro de cidadãos e gestores públicos")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UsuarioService usuarioService;

    @PostMapping("/login")
    @Operation(
            summary = "Autenticar usuário e gerar token JWT",
            description = "Valida as credenciais de e-mail e senha e retorna um token JWT Bearer com duração de 8 horas contendo o perfil de acesso (ROLE_CIDADAO ou ROLE_GESTOR)."
    )
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        String emailNormalizado = dto.email().trim().toLowerCase();
        var authToken = new UsernamePasswordAuthenticationToken(emailNormalizado, dto.senha());

        Authentication authentication = authenticationManager.authenticate(authToken);
        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();

        String tokenJWT = tokenService.gerarToken(usuarioAutenticado);
        TokenResponseDTO response = TokenResponseDTO.of(
                tokenJWT,
                tokenService.calcularDataExpiracao(),
                UsuarioResponseDTO.fromEntity(usuarioAutenticado)
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/registrar")
    @Operation(
            summary = "Registrar novo usuário na plataforma",
            description = "Cadastra um novo Cidadão (ROLE_CIDADAO) ou Gestor Público (ROLE_GESTOR) aplicando hash criptográfico BCrypt na senha."
    )
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO criado = usuarioService.criar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/usuarios/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

}

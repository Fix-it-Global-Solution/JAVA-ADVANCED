package com.fixit.api.controller;

import com.fixit.api.dto.LoginRequestDTO;
import com.fixit.api.dto.TokenResponseDTO;
import com.fixit.api.dto.UsuarioRequestDTO;
import com.fixit.api.dto.UsuarioResponseDTO;
import com.fixit.api.model.Usuario;
import com.fixit.api.security.TokenService;
import com.fixit.api.service.UsuarioService;
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
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UsuarioService usuarioService;

    @PostMapping("/login")
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
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO criado = usuarioService.criar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/usuarios/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

}


package com.fixit.api.dto;

import java.time.Instant;

public record TokenResponseDTO(
        String token,
        String tipo,
        Instant expiraEm,
        UsuarioResponseDTO usuario
) {
    public static TokenResponseDTO of(String token, Instant expiraEm, UsuarioResponseDTO usuario) {
        return new TokenResponseDTO(token, "Bearer", expiraEm, usuario);
    }
}


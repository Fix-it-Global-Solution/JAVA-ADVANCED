package com.fixit.api.dto;

import com.fixit.api.model.Usuario;
import com.fixit.api.model.enums.PerfilUsuario;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        PerfilUsuario perfil,
        LocalDateTime dataCadastro
) {
    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getDataCadastro()
        );
    }
}


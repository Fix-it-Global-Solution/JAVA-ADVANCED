package com.fixit.api.service;

import com.fixit.api.dto.UsuarioRequestDTO;
import com.fixit.api.dto.UsuarioResponseDTO;
import com.fixit.api.exception.BusinessException;
import com.fixit.api.exception.ResourceNotFoundException;
import com.fixit.api.model.Usuario;
import com.fixit.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {
        String emailNormalizado = dto.email().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new BusinessException("Já existe um usuário cadastrado com o e-mail: " + emailNormalizado);
        }

        Usuario usuario = Usuario.builder()
                .nome(dto.nome().trim())
                .email(emailNormalizado)
                .senha(passwordEncoder.encode(dto.senha()))
                .perfil(dto.perfil())
                .build();

        Usuario salvo = usuarioRepository.save(usuario);
        return UsuarioResponseDTO.fromEntity(salvo);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = buscarEntidadePorId(id);
        return UsuarioResponseDTO.fromEntity(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarEntidadePorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
    }

}

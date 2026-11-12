package com.fixit.api.dto;

import com.fixit.api.model.Ocorrencia;
import com.fixit.api.model.enums.StatusOcorrencia;

import java.time.LocalDateTime;

public record OcorrenciaResponseDTO(
        Long id,
        String titulo,
        String descricao,
        Double latitude,
        Double longitude,
        String enderecoReferencia,
        String fotoUrl,
        StatusOcorrencia status,
        LocalDateTime dataAbertura,
        LocalDateTime dataAtualizacao,
        String autorNome
) {
    public static OcorrenciaResponseDTO fromEntity(Ocorrencia ocorrencia) {
        return new OcorrenciaResponseDTO(
                ocorrencia.getId(),
                ocorrencia.getTitulo(),
                ocorrencia.getDescricao(),
                ocorrencia.getLatitude(),
                ocorrencia.getLongitude(),
                ocorrencia.getEnderecoReferencia(),
                ocorrencia.getFotoUrl(),
                ocorrencia.getStatus(),
                ocorrencia.getDataAbertura(),
                ocorrencia.getDataAtualizacao(),
                ocorrencia.getUsuario() != null ? ocorrencia.getUsuario().getNome() : null
        );
    }
}


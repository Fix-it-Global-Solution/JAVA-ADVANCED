package com.fixit.api.messaging;

import com.fixit.api.model.Ocorrencia;

import java.time.LocalDateTime;

public record OcorrenciaTriagemEventDTO(
        Long ocorrenciaId,
        String titulo,
        String descricao,
        Double latitude,
        Double longitude,
        String enderecoReferencia,
        String autorEmail,
        LocalDateTime dataEvento
) {
    public static OcorrenciaTriagemEventDTO fromEntity(Ocorrencia ocorrencia) {
        return new OcorrenciaTriagemEventDTO(
                ocorrencia.getId(),
                ocorrencia.getTitulo(),
                ocorrencia.getDescricao(),
                ocorrencia.getLatitude(),
                ocorrencia.getLongitude(),
                ocorrencia.getEnderecoReferencia(),
                ocorrencia.getUsuario() != null ? ocorrencia.getUsuario().getEmail() : null,
                LocalDateTime.now()
        );
    }
}


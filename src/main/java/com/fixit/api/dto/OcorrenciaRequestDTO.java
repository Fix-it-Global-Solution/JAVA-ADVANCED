package com.fixit.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OcorrenciaRequestDTO(
        @NotBlank(message = "O título da ocorrência é obrigatório.")
        @Size(max = 100, message = "O título deve ter no máximo 100 caracteres.")
        String titulo,

        @NotBlank(message = "A descrição da ocorrência é obrigatória.")
        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.")
        String descricao,

        @NotNull(message = "A latitude é obrigatória.")
        @DecimalMin(value = "-90.0", message = "A latitude deve ser maior ou igual a -90.0.")
        @DecimalMax(value = "90.0", message = "A latitude deve ser menor ou igual a 90.0.")
        Double latitude,

        @NotNull(message = "A longitude é obrigatória.")
        @DecimalMin(value = "-180.0", message = "A longitude deve ser maior ou igual a -180.0.")
        @DecimalMax(value = "180.0", message = "A longitude deve ser menor ou igual a 180.0.")
        Double longitude,

        @NotBlank(message = "O endereço de referência é obrigatório.")
        @Size(max = 255, message = "O endereço de referência deve ter no máximo 255 caracteres.")
        String enderecoReferencia,

        @Size(max = 500, message = "A URL da foto deve ter no máximo 500 caracteres.")
        String fotoUrl,

        @NotNull(message = "O ID do usuário autor (usuarioId) é obrigatório.")
        Long usuarioId
) {
}


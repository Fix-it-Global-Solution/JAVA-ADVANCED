package com.fixit.api.dto;

import com.fixit.api.model.enums.StatusOcorrencia;
import jakarta.validation.constraints.NotNull;

public record OcorrenciaStatusUpdateDTO(
        @NotNull(message = "O novo status (novoStatus) é obrigatório.")
        StatusOcorrencia novoStatus
) {
}


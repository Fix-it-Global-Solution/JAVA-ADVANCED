package com.fixit.api.dto;

import com.fixit.api.model.enums.NivelUrgencia;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record OrdemServicoRequestDTO(
        @NotNull(message = "O ID da ocorrência (ocorrenciaId) é obrigatório.")
        Long ocorrenciaId,

        @NotNull(message = "O nível de urgência é obrigatório.")
        NivelUrgencia nivelUrgencia,

        @NotBlank(message = "A equipe responsável é obrigatória.")
        @Size(max = 150, message = "A equipe responsável deve ter no máximo 150 caracteres.")
        String equipeResponsavel,

        @NotNull(message = "A data prevista de conclusão é obrigatória.")
        @FutureOrPresent(message = "A data prevista de conclusão deve ser a data atual ou futura.")
        LocalDate dataPrevistaConclusao
) {
}


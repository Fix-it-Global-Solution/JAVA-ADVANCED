package com.fixit.api.dto;

import com.fixit.api.model.OrdemServico;
import com.fixit.api.model.enums.NivelUrgencia;

import java.time.LocalDate;

public record OrdemServicoResponseDTO(
        Long id,
        String protocolo,
        NivelUrgencia nivelUrgencia,
        String parecerTecnicoIa,
        String equipeResponsavel,
        LocalDate dataPrevistaConclusao,
        Long ocorrenciaId
) {
    public static OrdemServicoResponseDTO fromEntity(OrdemServico ordemServico) {
        return new OrdemServicoResponseDTO(
                ordemServico.getId(),
                ordemServico.getProtocolo(),
                ordemServico.getNivelUrgencia(),
                ordemServico.getParecerTecnicoIa(),
                ordemServico.getEquipeResponsavel(),
                ordemServico.getDataPrevistaConclusao(),
                ordemServico.getOcorrencia() != null ? ordemServico.getOcorrencia().getId() : null
        );
    }
}


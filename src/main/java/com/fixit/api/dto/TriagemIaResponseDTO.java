package com.fixit.api.dto;

import com.fixit.api.model.enums.NivelUrgencia;

import java.time.LocalDate;

public record TriagemIaResponseDTO(
        Long ocorrenciaId,
        String tituloOcorrencia,
        NivelUrgencia nivelUrgenciaSugerido,
        String equipeResponsavelSugerida,
        int prazoEstimadoDias,
        LocalDate dataPrevistaConclusaoSugerida,
        String parecerTecnicoIa,
        String modoExecucao
) {
}


package com.fixit.api.service;

import com.fixit.api.dto.OrdemServicoRequestDTO;
import com.fixit.api.dto.OrdemServicoResponseDTO;
import com.fixit.api.dto.TriagemIaResponseDTO;
import com.fixit.api.exception.BusinessException;
import com.fixit.api.exception.ResourceNotFoundException;
import com.fixit.api.model.Ocorrencia;
import com.fixit.api.model.OrdemServico;
import com.fixit.api.model.enums.StatusOcorrencia;
import com.fixit.api.repository.OrdemServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final OcorrenciaService ocorrenciaService;
    private final TriagemIaService triagemIaService;

    @Transactional
    public OrdemServicoResponseDTO criar(OrdemServicoRequestDTO dto) {
        Ocorrencia ocorrencia = validarOcorrenciaElegivelParaOs(dto.ocorrenciaId());

        String parecerIa = triagemIaService.gerarParecerTecnico(
                ocorrencia,
                dto.nivelUrgencia(),
                dto.equipeResponsavel().trim()
        );

        OrdemServico ordemServico = ordemServicoRepository.findByOcorrenciaId(ocorrencia.getId())
                .orElseGet(() -> OrdemServico.builder()
                        .protocolo(UUID.randomUUID().toString())
                        .ocorrencia(ocorrencia)
                        .build());

        ordemServico.setNivelUrgencia(dto.nivelUrgencia());
        ordemServico.setParecerTecnicoIa(parecerIa);
        ordemServico.setEquipeResponsavel(dto.equipeResponsavel().trim());
        ordemServico.setDataPrevistaConclusao(dto.dataPrevistaConclusao());

        ocorrencia.setStatus(StatusOcorrencia.EM_EXECUCAO);
        ocorrencia.vincularOrdemServico(ordemServico);

        OrdemServico salva = ordemServicoRepository.save(ordemServico);
        return OrdemServicoResponseDTO.fromEntity(salva);
    }

    @Transactional
    public OrdemServicoResponseDTO despacharAutomaticamenteComIa(Long ocorrenciaId) {
        Ocorrencia ocorrencia = validarOcorrenciaElegivelParaOs(ocorrenciaId);

        TriagemIaResponseDTO triagemIa = triagemIaService.analisarOcorrencia(ocorrencia);

        OrdemServico ordemServico = ordemServicoRepository.findByOcorrenciaId(ocorrencia.getId())
                .orElseGet(() -> OrdemServico.builder()
                        .protocolo(UUID.randomUUID().toString())
                        .ocorrencia(ocorrencia)
                        .build());

        ordemServico.setNivelUrgencia(triagemIa.nivelUrgenciaSugerido());
        ordemServico.setParecerTecnicoIa(triagemIa.parecerTecnicoIa());
        ordemServico.setEquipeResponsavel(triagemIa.equipeResponsavelSugerida());
        ordemServico.setDataPrevistaConclusao(triagemIa.dataPrevistaConclusaoSugerida());

        ocorrencia.setStatus(StatusOcorrencia.EM_EXECUCAO);
        ocorrencia.vincularOrdemServico(ordemServico);

        OrdemServico salva = ordemServicoRepository.save(ordemServico);
        return OrdemServicoResponseDTO.fromEntity(salva);
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponseDTO buscarPorProtocolo(String protocolo) {
        OrdemServico ordemServico = ordemServicoRepository.findByProtocolo(protocolo)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de Serviço não encontrada para o protocolo: " + protocolo));
        return OrdemServicoResponseDTO.fromEntity(ordemServico);
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponseDTO buscarPorOcorrenciaId(Long ocorrenciaId) {
        OrdemServico ordemServico = ordemServicoRepository.findByOcorrenciaId(ocorrenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de Serviço não encontrada para a ocorrência de ID: " + ocorrenciaId));
        return OrdemServicoResponseDTO.fromEntity(ordemServico);
    }

    private Ocorrencia validarOcorrenciaElegivelParaOs(Long ocorrenciaId) {
        Ocorrencia ocorrencia = ocorrenciaService.buscarEntidadePorId(ocorrenciaId);

        if (ocorrencia.getStatus() == StatusOcorrencia.CANCELADO
                || ocorrencia.getStatus() == StatusOcorrencia.CONCLUIDO) {
            throw new BusinessException("Não é possível emitir Ordem de Serviço para ocorrência com status: " + ocorrencia.getStatus());
        }

        return ocorrencia;
    }

}

package com.fixit.api.service;

import com.fixit.api.dto.OrdemServicoRequestDTO;
import com.fixit.api.dto.OrdemServicoResponseDTO;
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

    @Transactional
    public OrdemServicoResponseDTO criar(OrdemServicoRequestDTO dto) {
        Ocorrencia ocorrencia = ocorrenciaService.buscarEntidadePorId(dto.ocorrenciaId());

        if (ordemServicoRepository.findByOcorrenciaId(ocorrencia.getId()).isPresent()) {
            throw new BusinessException("Já existe uma Ordem de Serviço vinculada à ocorrência de ID: " + ocorrencia.getId());
        }

        if (ocorrencia.getStatus() == StatusOcorrencia.CANCELADO
                || ocorrencia.getStatus() == StatusOcorrencia.CONCLUIDO) {
            throw new BusinessException("Não é possível emitir Ordem de Serviço para ocorrência com status: " + ocorrencia.getStatus());
        }

        OrdemServico ordemServico = OrdemServico.builder()
                .protocolo(UUID.randomUUID().toString())
                .nivelUrgencia(dto.nivelUrgencia())
                .equipeResponsavel(dto.equipeResponsavel().trim())
                .dataPrevistaConclusao(dto.dataPrevistaConclusao())
                .ocorrencia(ocorrencia)
                .build();

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

}


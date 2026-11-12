package com.fixit.api.service;

import com.fixit.api.dto.OcorrenciaRequestDTO;
import com.fixit.api.dto.OcorrenciaResponseDTO;
import com.fixit.api.dto.OcorrenciaStatusUpdateDTO;
import com.fixit.api.dto.TriagemIaResponseDTO;
import com.fixit.api.exception.BusinessException;
import com.fixit.api.exception.ResourceNotFoundException;
import com.fixit.api.messaging.OcorrenciaEventPublisher;
import com.fixit.api.model.Ocorrencia;
import com.fixit.api.model.Usuario;
import com.fixit.api.model.enums.StatusOcorrencia;
import com.fixit.api.repository.OcorrenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OcorrenciaService {

    private final OcorrenciaRepository ocorrenciaRepository;
    private final UsuarioService usuarioService;
    private final TriagemIaService triagemIaService;
    private final OcorrenciaEventPublisher ocorrenciaEventPublisher;

    @Transactional
    public OcorrenciaResponseDTO criar(OcorrenciaRequestDTO dto) {
        Usuario autor = usuarioService.buscarEntidadePorId(dto.usuarioId());

        Ocorrencia ocorrencia = Ocorrencia.builder()
                .titulo(dto.titulo().trim())
                .descricao(dto.descricao().trim())
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .enderecoReferencia(dto.enderecoReferencia().trim())
                .fotoUrl(dto.fotoUrl())
                .status(StatusOcorrencia.ABERTO)
                .usuario(autor)
                .build();

        Ocorrencia salva = ocorrenciaRepository.save(ocorrencia);
        ocorrenciaEventPublisher.publicarEventoTriagem(salva);

        return OcorrenciaResponseDTO.fromEntity(salva);
    }

    @Transactional(readOnly = true)
    public Page<OcorrenciaResponseDTO> listarPaginado(StatusOcorrencia status, Pageable pageable) {
        Page<Ocorrencia> pagina = (status != null)
                ? ocorrenciaRepository.findByStatus(status, pageable)
                : ocorrenciaRepository.findAll(pageable);

        return pagina.map(OcorrenciaResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<OcorrenciaResponseDTO> listarPorUsuario(Long usuarioId, Pageable pageable) {
        usuarioService.buscarEntidadePorId(usuarioId);
        return ocorrenciaRepository.findByUsuarioId(usuarioId, pageable)
                .map(OcorrenciaResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public OcorrenciaResponseDTO buscarPorId(Long id) {
        Ocorrencia ocorrencia = buscarEntidadePorId(id);
        return OcorrenciaResponseDTO.fromEntity(ocorrencia);
    }

    @Transactional
    public TriagemIaResponseDTO realizarTriagemIa(Long id) {
        Ocorrencia ocorrencia = buscarEntidadePorId(id);

        if (ocorrencia.getStatus() == StatusOcorrencia.CANCELADO
                || ocorrencia.getStatus() == StatusOcorrencia.CONCLUIDO) {
            throw new BusinessException("Não é possível realizar triagem de IA em ocorrência com status: " + ocorrencia.getStatus());
        }

        if (ocorrencia.getStatus() == StatusOcorrencia.ABERTO) {
            ocorrencia.setStatus(StatusOcorrencia.EM_ANALISE);
            ocorrenciaRepository.save(ocorrencia);
        }

        return triagemIaService.analisarOcorrencia(ocorrencia);
    }

    @Transactional
    public OcorrenciaResponseDTO atualizarStatus(Long id, OcorrenciaStatusUpdateDTO dto) {
        Ocorrencia ocorrencia = buscarEntidadePorId(id);

        if (ocorrencia.getStatus() == StatusOcorrencia.CANCELADO) {
            throw new BusinessException("Não é permitido alterar o status de uma ocorrência já cancelada.");
        }

        ocorrencia.setStatus(dto.novoStatus());
        Ocorrencia atualizada = ocorrenciaRepository.save(ocorrencia);
        return OcorrenciaResponseDTO.fromEntity(atualizada);
    }

    @Transactional
    public void excluir(Long id) {
        Ocorrencia ocorrencia = buscarEntidadePorId(id);

        if (ocorrencia.getStatus() == StatusOcorrencia.EM_EXECUCAO) {
            throw new BusinessException("Não é possível excluir uma ocorrência que já está em execução por uma equipe técnica.");
        }

        ocorrenciaRepository.delete(ocorrencia);
    }

    @Transactional(readOnly = true)
    public Ocorrencia buscarEntidadePorId(Long id) {
        return ocorrenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ocorrência não encontrada com ID: " + id));
    }

}

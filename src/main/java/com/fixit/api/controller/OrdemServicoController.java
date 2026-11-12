package com.fixit.api.controller;

import com.fixit.api.dto.OrdemServicoRequestDTO;
import com.fixit.api.dto.OrdemServicoResponseDTO;
import com.fixit.api.service.OrdemServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/ordens-servico")
@RequiredArgsConstructor
@Tag(name = "4. Ordens de Serviço (OS)", description = "Emissão, despacho automatizado com Google Gemini (Spring AI) e acompanhamento por protocolo")
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    @PostMapping
    @Operation(
            summary = "Emitir Ordem de Serviço enriquecida com parecer do Google Gemini (Exclusivo ROLE_GESTOR)",
            description = "Cria ou confirma uma Ordem de Serviço vinculada à ocorrência, gerando protocolo UUID único e parecer técnico elaborado via Google Gemini (gemini-1.5-flash)."
    )
    public ResponseEntity<OrdemServicoResponseDTO> criar(@Valid @RequestBody OrdemServicoRequestDTO dto) {
        OrdemServicoResponseDTO criada = ordemServicoService.criar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/protocolo/{protocolo}")
                .buildAndExpand(criada.protocolo())
                .toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @PostMapping("/despacho-ia/{ocorrenciaId}")
    @Operation(
            summary = "Despachar Ordem de Serviço 100% automatizada com Google Gemini (Exclusivo ROLE_GESTOR)",
            description = "Utiliza o Spring AI + Google Gemini (gemini-1.5-flash) para definir automaticamente urgência, equipe responsável, prazo estimado e parecer técnico, mudando o status da ocorrência para EM_EXECUCAO."
    )
    public ResponseEntity<OrdemServicoResponseDTO> despacharAutomaticamenteComIa(@PathVariable Long ocorrenciaId) {
        OrdemServicoResponseDTO criada = ordemServicoService.despacharAutomaticamenteComIa(ocorrenciaId);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/ordens-servico/protocolo/{protocolo}")
                .buildAndExpand(criada.protocolo())
                .toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @GetMapping("/protocolo/{protocolo}")
    @Operation(
            summary = "Consultar Ordem de Serviço pelo protocolo UUID",
            description = "Retorna os detalhes da Ordem de Serviço, equipe destacada, prazo e parecer técnico do Google Gemini a partir do protocolo."
    )
    public ResponseEntity<OrdemServicoResponseDTO> buscarPorProtocolo(@PathVariable String protocolo) {
        return ResponseEntity.ok(ordemServicoService.buscarPorProtocolo(protocolo));
    }

    @GetMapping("/ocorrencia/{ocorrenciaId}")
    @Operation(
            summary = "Consultar Ordem de Serviço pelo ID da Ocorrência",
            description = "Retorna a Ordem de Serviço vinculada a uma ocorrência específica (incluindo a OS gerada assincronamente via RabbitMQ)."
    )
    public ResponseEntity<OrdemServicoResponseDTO> buscarPorOcorrenciaId(@PathVariable Long ocorrenciaId) {
        return ResponseEntity.ok(ordemServicoService.buscarPorOcorrenciaId(ocorrenciaId));
    }

}

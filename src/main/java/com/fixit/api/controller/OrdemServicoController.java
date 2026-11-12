package com.fixit.api.controller;

import com.fixit.api.dto.OrdemServicoRequestDTO;
import com.fixit.api.dto.OrdemServicoResponseDTO;
import com.fixit.api.service.OrdemServicoService;
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
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    @PostMapping
    public ResponseEntity<OrdemServicoResponseDTO> criar(@Valid @RequestBody OrdemServicoRequestDTO dto) {
        OrdemServicoResponseDTO criada = ordemServicoService.criar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/protocolo/{protocolo}")
                .buildAndExpand(criada.protocolo())
                .toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @GetMapping("/protocolo/{protocolo}")
    public ResponseEntity<OrdemServicoResponseDTO> buscarPorProtocolo(@PathVariable String protocolo) {
        return ResponseEntity.ok(ordemServicoService.buscarPorProtocolo(protocolo));
    }

}


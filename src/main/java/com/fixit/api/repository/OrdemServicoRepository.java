package com.fixit.api.repository;

import com.fixit.api.model.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    Optional<OrdemServico> findByProtocolo(String protocolo);

    Optional<OrdemServico> findByOcorrenciaId(Long ocorrenciaId);

}


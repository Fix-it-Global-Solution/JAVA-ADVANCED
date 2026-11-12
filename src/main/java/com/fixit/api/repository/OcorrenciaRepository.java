package com.fixit.api.repository;

import com.fixit.api.model.Ocorrencia;
import com.fixit.api.model.enums.StatusOcorrencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {

    @Override
    @EntityGraph(attributePaths = {"usuario", "ordemServico"})
    Page<Ocorrencia> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"usuario", "ordemServico"})
    Optional<Ocorrencia> findById(Long id);

    @EntityGraph(attributePaths = {"usuario", "ordemServico"})
    List<Ocorrencia> findByStatus(StatusOcorrencia status);

    @EntityGraph(attributePaths = {"usuario", "ordemServico"})
    Page<Ocorrencia> findByStatus(StatusOcorrencia status, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "ordemServico"})
    Page<Ocorrencia> findByUsuarioId(Long usuarioId, Pageable pageable);

}

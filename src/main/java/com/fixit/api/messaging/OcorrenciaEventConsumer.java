package com.fixit.api.messaging;

import com.fixit.api.config.RabbitMQConfig;
import com.fixit.api.dto.TriagemIaResponseDTO;
import com.fixit.api.model.Ocorrencia;
import com.fixit.api.model.OrdemServico;
import com.fixit.api.model.enums.StatusOcorrencia;
import com.fixit.api.repository.OcorrenciaRepository;
import com.fixit.api.repository.OrdemServicoRepository;
import com.fixit.api.service.TriagemIaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OcorrenciaEventConsumer {

    private final OcorrenciaRepository ocorrenciaRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final TriagemIaService triagemIaService;

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumirEventoTriagem(OcorrenciaTriagemEventDTO evento) {
        log.info("[RabbitMQ Consumer] Mensagem recebida da fila '{}': Ocorrência ID={} | Título='{}' | Autor='{}'",
                RabbitMQConfig.QUEUE_NAME,
                evento.ocorrenciaId(),
                evento.titulo(),
                evento.autorEmail());

        executarTriagemEGerarOrdemServico(evento.ocorrenciaId(), "RABBITMQ_ASYNC_QUEUE");
    }

    @Transactional
    public void executarTriagemEGerarOrdemServico(Long ocorrenciaId, String origemFluxo) {
        Ocorrencia ocorrencia = ocorrenciaRepository.findById(ocorrenciaId).orElse(null);
        if (ocorrencia == null) {
            log.warn("[Triagem Zeladoria] Ocorrência ID={} não encontrada no banco de dados. Evento ignorado.", ocorrenciaId);
            return;
        }

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

        ocorrencia.setStatus(StatusOcorrencia.EM_ANALISE);
        ocorrencia.vincularOrdemServico(ordemServico);

        OrdemServico osSalva = ordemServicoRepository.save(ordemServico);
        ocorrenciaRepository.save(ocorrencia);

        log.info("[Triagem Concluída | Origem: {} | Motor IA: {}] Ocorrência ID={} atualizada para status '{}' | Ordem de Serviço gerada com Protocolo='{}' | Urgência='{}' | Equipe='{}'",
                origemFluxo,
                triagemIa.modoExecucao(),
                ocorrencia.getId(),
                ocorrencia.getStatus(),
                osSalva.getProtocolo(),
                osSalva.getNivelUrgencia(),
                osSalva.getEquipeResponsavel());
    }

}


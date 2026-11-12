package com.fixit.api.messaging;

import com.fixit.api.config.RabbitMQConfig;
import com.fixit.api.model.Ocorrencia;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OcorrenciaEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final OcorrenciaEventConsumer ocorrenciaEventConsumer;

    @Value("${app.rabbitmq.exchange:" + RabbitMQConfig.EXCHANGE_NAME + "}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key:" + RabbitMQConfig.ROUTING_KEY + "}")
    private String routingKey;

    public void publicarEventoTriagem(Ocorrencia ocorrencia) {
        OcorrenciaTriagemEventDTO evento = OcorrenciaTriagemEventDTO.fromEntity(ocorrencia);

        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, evento);
            log.info("[RabbitMQ Publisher] Evento de triagem publicado com sucesso na exchange '{}' (routingKey='{}') para a Ocorrência ID={}.",
                    exchangeName, routingKey, ocorrencia.getId());
        } catch (Exception ex) {
            log.warn("[RabbitMQ Contingência] Broker RabbitMQ indisponível no momento ({}). Executando triagem via Google Gemini / Fallback de forma síncrona para a Ocorrência ID={}.",
                    ex.getMessage(), ocorrencia.getId());
            ocorrenciaEventConsumer.executarTriagemEGerarOrdemServico(
                    ocorrencia.getId(),
                    "CONTINGENCIA_SINCRONA_SEM_BROKER"
            );
        }
    }

}


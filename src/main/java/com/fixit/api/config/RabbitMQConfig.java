package com.fixit.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "fixit.zeladoria.exchange";
    public static final String QUEUE_NAME = "fila-triagem-zeladoria";
    public static final String ROUTING_KEY = "fixit.ocorrencia.triagem";

    @Value("${app.rabbitmq.exchange:" + EXCHANGE_NAME + "}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue:" + QUEUE_NAME + "}")
    private String queueName;

    @Value("${app.rabbitmq.routing-key:" + ROUTING_KEY + "}")
    private String routingKey;

    @Bean
    public Queue filaTriagemZeladoria() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public TopicExchange zeladoriaExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Binding bindingTriagemZeladoria(Queue filaTriagemZeladoria, TopicExchange zeladoriaExchange) {
        return BindingBuilder.bind(filaTriagemZeladoria)
                .to(zeladoriaExchange)
                .with(routingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter jackson2JsonMessageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter);
        return rabbitTemplate;
    }

}


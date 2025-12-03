package com.ridesharing.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@Configuration
public class KafkaFallback {
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        // simple stub that returns null. In real dev, inject an actual KafkaTemplate.
        return new KafkaTemplate<>(new org.springframework.kafka.core.DefaultKafkaProducerFactory<>(java.util.Collections.emptyMap())) {
            @Override
            public CompletableFuture<SendResult<String, String>> send(String topic, String data) {
                System.out.println("[DEV-KAFKA-STUB] send to topic=" + topic + " payload=" + data);
                return super.send(topic, data); // safe no-ops
            }
        };
    }
}

package com.idnaheim.lifem.messaging;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class TransactionEventProducer {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
    private final String topic;

    public TransactionEventProducer(
            KafkaTemplate<String, TransactionEvent> kafkaTemplate,
            @Value("${kafka.topic.transactions}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(TransactionEvent event) {
        CompletableFuture<SendResult<String, TransactionEvent>> future =
                kafkaTemplate.send(topic, event.referenceNo(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish transaction event [{}] referenceNo={}: {}",
                        event.eventType(), event.referenceNo(), ex.getMessage());
            } else {
                log.info("Published transaction event [{}] referenceNo={} → {}@{}",
                        event.eventType(),
                        event.referenceNo(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}

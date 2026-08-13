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
    private final String transactionsTopic;

    public TransactionEventProducer(
            KafkaTemplate<String, TransactionEvent> kafkaTemplate,
            @Value("${kafka.topic.transactions:lifem.transactions}") String transactionsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.transactionsTopic = transactionsTopic;
    }

    public void publish(TransactionEvent event) {
        CompletableFuture<SendResult<String, TransactionEvent>> future =
                kafkaTemplate.send(transactionsTopic, event.referenceNo(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish transaction event [ref={}]: {}", event.referenceNo(), ex.getMessage());
            } else {
                log.info("Published transaction event [ref={}, partition={}, offset={}]",
                        event.referenceNo(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}

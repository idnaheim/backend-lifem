package com.idnaheim.lifem.messaging;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class TransactionEventConsumer {

    // Thread-safe list; acts as an in-memory event log
    @Getter
    private final CopyOnWriteArrayList<TransactionEvent> receivedEvents = new CopyOnWriteArrayList<>();

    @KafkaListener(
            topics = "${kafka.topic.transactions:lifem.transactions}",
            groupId = "${spring.kafka.consumer.group-id:lifem-consumer-group}"
    )
    public void consume(TransactionEvent event) {
        log.info("Consumed transaction event [ref={}, type={}, amount={}]",
                event.referenceNo(), event.type(), event.amount());
        receivedEvents.add(event);
    }

    public List<TransactionEvent> getEvents() {
        // Return newest first
        List<TransactionEvent> copy = new ArrayList<>(receivedEvents);
        Collections.reverse(copy);
        return copy;
    }
}

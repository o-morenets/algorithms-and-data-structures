package com.codeus.outbox.orderservice.kafka;

import com.codeus.outbox.orderservice.entity.OutboxEvent;
import com.codeus.outbox.orderservice.entity.OutboxEventStatus;
import com.codeus.outbox.orderservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxEventRepository outboxRepository;

    public void publish(OutboxEvent event) {
        KafkaPublisher.log.info("\n\n➡️ Publishing event to Kafka topic=order-created");

        sendEventToKafka(event);

        int random = ThreadLocalRandom.current().nextInt(1, 101); // 1–100
        boolean shouldDuplicate = random <= 70;
        if (shouldDuplicate) {
            log.info("Duplicating event with id={}", event.getId().toString());
            sendEventToKafka(event);
        }
    }

    private void sendEventToKafka(OutboxEvent event) {
        kafkaTemplate.send("order-created", event.getId().toString(), event.getPayload())
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        event.setStatus(OutboxEventStatus.SENT);
                        event.setProcessedAt(Instant.now());
                    } else {
                        event.setStatus(OutboxEventStatus.FAILED);
                    }

                    try {
                        outboxRepository.save(event);
                    } catch (Exception e) {
                        log.error("Failed to save event with id={} to database: {}", event.getId(), e.getMessage());
                        throw new RuntimeException(e);
                    }
                });
    }
}

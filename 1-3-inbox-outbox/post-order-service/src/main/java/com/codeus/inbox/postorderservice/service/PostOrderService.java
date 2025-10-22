package com.codeus.inbox.postorderservice.service;

import com.codeus.inbox.postorderservice.entity.InboxEvent;
import com.codeus.inbox.postorderservice.entity.InboxEventStatus;
import com.codeus.inbox.postorderservice.repository.InboxEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostOrderService {

    private final InboxEventRepository inboxRepository;

    public long count() {
        return inboxRepository.count();
    }

    public void process(InboxEvent event) {
        int random = ThreadLocalRandom.current().nextInt(1, 11);
        if (random <= 4) { // 40% chance of failure
            throw new RuntimeException("Simulated random failure for testing");
        }
        // some business logic...
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processInbox() {
        log.info("Processing inbox events with status FAILED");

        inboxRepository.findByStatus(InboxEventStatus.FAILED)
                .forEach(event -> {
                    UUID eventId = UUID.fromString(event.getId().toString());
                    log.info("\n\nProcessing Kafka message with key={}", eventId);

                    try {
                        // Process business logic
                        process(event);

                        event.setStatus(InboxEventStatus.PROCESSED);
                        event.setProcessedAt(Instant.now());
                        inboxRepository.save(event);
                        log.info("\n\nSuccessfully processed event id={}", event.getId());
                    } catch (Exception e) {
                        log.error("\n\nError while processing event - {}", e.getMessage(), e);
                        event.setStatus(InboxEventStatus.FAILED);
                        inboxRepository.save(event);
                        log.warn("\n\nEvent id={} marked as FAILED", event.getId());
                    }
                });
    }
}

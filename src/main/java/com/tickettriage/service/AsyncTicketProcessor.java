package com.tickettriage.service;

import com.tickettriage.dto.ClassificationResult;
import com.tickettriage.event.TicketClassificationEvent;
import com.tickettriage.model.Ticket;
import com.tickettriage.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncTicketProcessor {

    private final TicketRepository ticketRepository;
    private final GeminiClassificationService geminiService;

    /**
     * CONSUMER: Listens for TicketClassificationEvent and processes tickets
     * asynchronously.
     * This method runs in a separate thread pool, allowing the API to return
     * immediately.
     */
    @Async("ticketProcessorExecutor")
    @EventListener
    @Transactional
    public void handleTicketClassification(TicketClassificationEvent event) {
        Long ticketId = event.getTicketId();
        log.info("Processing ticket ID: {} in thread: {}", ticketId, Thread.currentThread().getName());

        try {
            // 1. Fetch the ticket
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));

            // 2. Update status to PROCESSING
            ticket.setStatus(Ticket.TicketStatus.PROCESSING);
            ticketRepository.save(ticket);
            log.info("Ticket {} status updated to PROCESSING", ticketId);

            // 3. Call Gemini AI for classification
            ClassificationResult result = geminiService.classify(ticket);
            log.info("Ticket {} classified - Category: {}, Priority: {}, Sentiment: {}",
                    ticketId, result.getCategory(), result.getPriority(), result.getSentiment());

            // 4. Update ticket with classification results
            ticket.setCategory(result.getCategory());
            ticket.setPriority(result.getPriority());
            ticket.setSentiment(result.getSentiment());
            ticket.setStatus(Ticket.TicketStatus.CLASSIFIED);
            ticketRepository.save(ticket);

            log.info("Ticket {} successfully classified and saved", ticketId);

        } catch (Exception e) {
            log.error("Failed to process ticket ID: {}", ticketId, e);

            // Update ticket status to FAILED with error message
            ticketRepository.findById(ticketId).ifPresent(ticket -> {
                ticket.setStatus(Ticket.TicketStatus.FAILED);
                ticket.setErrorMessage(e.getMessage());
                ticketRepository.save(ticket);
            });
        }
    }
}

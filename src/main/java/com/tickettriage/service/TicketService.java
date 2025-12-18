package com.tickettriage.service;

import com.tickettriage.dto.TicketRequest;
import com.tickettriage.dto.TicketStatsResponse;
import com.tickettriage.event.TicketClassificationEvent;
import com.tickettriage.model.Ticket;
import com.tickettriage.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * PRODUCER: Creates a ticket and publishes an event for async classification.
     * Returns immediately with PENDING status.
     */
    @Transactional
    public Ticket createTicket(TicketRequest request) {
        log.info("Creating new ticket: {}", request.getSubject());

        // 1. Create and save ticket with PENDING status
        Ticket ticket = new Ticket();
        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setStatus(Ticket.TicketStatus.PENDING);
        ticket = ticketRepository.save(ticket);

        log.info("Ticket created with ID: {}", ticket.getId());

        // 2. PUBLISH EVENT for async processing (like sending to Kafka topic)
        eventPublisher.publishEvent(new TicketClassificationEvent(this, ticket.getId()));
        log.info("Published classification event for ticket ID: {}", ticket.getId());

        // 3. Return immediately - user sees "Ticket Received"
        return ticket;
    }

    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with ID: " + id));
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public List<Ticket> getTicketsByStatus(Ticket.TicketStatus status) {
        return ticketRepository.findByStatus(status);
    }

    public List<Ticket> getTicketsByCategory(Ticket.Category category) {
        return ticketRepository.findByCategory(category);
    }

    public List<Ticket> getTicketsByPriority(Ticket.Priority priority) {
        return ticketRepository.findByPriority(priority);
    }

    public TicketStatsResponse getStatistics() {
        TicketStatsResponse stats = new TicketStatsResponse();

        // Total tickets
        stats.setTotalTickets(ticketRepository.count());

        // By status
        Map<String, Long> byStatus = new HashMap<>();
        for (Ticket.TicketStatus status : Ticket.TicketStatus.values()) {
            byStatus.put(status.name(), ticketRepository.countByStatus(status));
        }
        stats.setByStatus(byStatus);

        // By category
        Map<String, Long> byCategory = new HashMap<>();
        for (Ticket.Category category : Ticket.Category.values()) {
            byCategory.put(category.name(), ticketRepository.countByCategory(category));
        }
        stats.setByCategory(byCategory);

        // By priority
        Map<String, Long> byPriority = new HashMap<>();
        for (Ticket.Priority priority : Ticket.Priority.values()) {
            byPriority.put(priority.name(), ticketRepository.countByPriority(priority));
        }
        stats.setByPriority(byPriority);

        return stats;
    }
}

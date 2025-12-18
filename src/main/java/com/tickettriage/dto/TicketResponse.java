package com.tickettriage.dto;

import com.tickettriage.model.Ticket;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketResponse {

    private Long id;
    private String subject;
    private String description;
    private Ticket.TicketStatus status;
    private Ticket.Category category;
    private Ticket.Priority priority;
    private Integer sentiment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String errorMessage;

    public static TicketResponse from(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setSubject(ticket.getSubject());
        response.setDescription(ticket.getDescription());
        response.setStatus(ticket.getStatus());
        response.setCategory(ticket.getCategory());
        response.setPriority(ticket.getPriority());
        response.setSentiment(ticket.getSentiment());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());
        response.setErrorMessage(ticket.getErrorMessage());
        return response;
    }
}

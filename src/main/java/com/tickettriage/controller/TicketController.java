package com.tickettriage.controller;

import com.tickettriage.dto.TicketRequest;
import com.tickettriage.dto.TicketResponse;
import com.tickettriage.dto.TicketStatsResponse;
import com.tickettriage.model.Ticket;
import com.tickettriage.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ticket Management", description = "APIs for managing customer support tickets and AI classification")
public class TicketController {

    private final TicketService ticketService;

    /**
     * Submit a new support ticket.
     * Returns immediately with PENDING status while AI processes it asynchronously.
     */
    @PostMapping
    @Operation(summary = "Create a new ticket", description = "Submit a new customer support ticket for AI classification. The ticket will be processed asynchronously.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ticket created successfully", content = @Content(schema = @Schema(implementation = TicketResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<TicketResponse> createTicket(
            @Parameter(description = "Ticket details including subject and description", required = true) @Valid @RequestBody TicketRequest request) {
        log.info("Received ticket creation request: {}", request.getSubject());
        Ticket ticket = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TicketResponse.from(ticket));
    }

    /**
     * Get a specific ticket by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by ID", description = "Retrieve a specific ticket with its classification status and results")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket found", content = @Content(schema = @Schema(implementation = TicketResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    public ResponseEntity<TicketResponse> getTicket(
            @Parameter(description = "Ticket ID", required = true, example = "1") @PathVariable Long id) {
        Ticket ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(TicketResponse.from(ticket));
    }

    /**
     * Get all tickets with optional filters.
     */
    @GetMapping
    @Operation(summary = "List all tickets", description = "Retrieve all tickets with optional filtering by status, category, or priority")
    @ApiResponse(responseCode = "200", description = "List of tickets retrieved successfully")
    public ResponseEntity<List<TicketResponse>> getAllTickets(
            @Parameter(description = "Filter by ticket status", example = "CLASSIFIED") @RequestParam(required = false) Ticket.TicketStatus status,
            @Parameter(description = "Filter by category", example = "BUG") @RequestParam(required = false) Ticket.Category category,
            @Parameter(description = "Filter by priority", example = "HIGH") @RequestParam(required = false) Ticket.Priority priority) {

        List<Ticket> tickets;

        if (status != null) {
            tickets = ticketService.getTicketsByStatus(status);
        } else if (category != null) {
            tickets = ticketService.getTicketsByCategory(category);
        } else if (priority != null) {
            tickets = ticketService.getTicketsByPriority(priority);
        } else {
            tickets = ticketService.getAllTickets();
        }

        List<TicketResponse> responses = tickets.stream()
                .map(TicketResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Get ticket classification statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<TicketStatsResponse> getStatistics() {
        return ResponseEntity.ok(ticketService.getStatistics());
    }
}

package com.tickettriage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketStatsResponse {

    private long totalTickets;
    private Map<String, Long> byStatus;
    private Map<String, Long> byCategory;
    private Map<String, Long> byPriority;
}

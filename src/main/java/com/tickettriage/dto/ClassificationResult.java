package com.tickettriage.dto;

import com.tickettriage.model.Ticket;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationResult {

    private Ticket.Category category;
    private Ticket.Priority priority;
    private Integer sentiment; // 1-10 scale
    private String reasoning; // Optional: AI's explanation
}

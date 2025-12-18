package com.tickettriage.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a new ticket needs to be classified.
 * This event is consumed asynchronously by the AI processor.
 */
@Getter
public class TicketClassificationEvent extends ApplicationEvent {

    private final Long ticketId;

    public TicketClassificationEvent(Object source, Long ticketId) {
        super(source);
        this.ticketId = ticketId;
    }
}

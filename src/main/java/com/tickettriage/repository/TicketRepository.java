package com.tickettriage.repository;

import com.tickettriage.model.Ticket;
import com.tickettriage.model.Ticket.Category;
import com.tickettriage.model.Ticket.Priority;
import com.tickettriage.model.Ticket.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByCategory(Category category);

    List<Ticket> findByPriority(Priority priority);

    List<Ticket> findByCategoryAndPriority(Category category, Priority priority);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = :status")
    long countByStatus(TicketStatus status);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.category = :category")
    long countByCategory(Category category);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.priority = :priority")
    long countByPriority(Priority priority);
}

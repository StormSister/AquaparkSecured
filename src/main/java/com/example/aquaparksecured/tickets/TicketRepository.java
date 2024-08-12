package com.example.aquaparksecured.tickets;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUserId(Long userId);
    List<Ticket> findByEmail(String email);

    Ticket findTicketById(Long ticketId);

    Optional<Ticket> findByQrCode(String qrCode);

    // methods
}
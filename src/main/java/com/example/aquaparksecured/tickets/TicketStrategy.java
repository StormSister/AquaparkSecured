package com.example.aquaparksecured.tickets;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface TicketStrategy {
    List<String> createTickets(String email, List<Map<String, Object>> ticketDetails, LocalDateTime purchaseDate, LocalDateTime expirationDate);
}
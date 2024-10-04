package com.example.aquaparksecured.tickets;

import com.example.aquaparksecured.price.PriceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class StandardTicketStrategy extends AbstractTicketStrategy {

    public StandardTicketStrategy(PriceRepository priceRepository, PdfService pdfService, TicketRepository ticketRepository) {
        super(priceRepository, pdfService, ticketRepository);
    }

    @Override
    public List<String> createTickets(String email, List<Map<String, Object>> ticketDetails, LocalDateTime purchaseDate, LocalDateTime expirationDate) {
        return super.createTickets(email, ticketDetails, purchaseDate, expirationDate, 1, 0);
    }
}
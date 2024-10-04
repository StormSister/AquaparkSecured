package com.example.aquaparksecured.tickets;

import com.example.aquaparksecured.price.PriceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;



@Service
public class SeniorTicketStrategy extends AbstractTicketStrategy {

    public SeniorTicketStrategy(PriceRepository priceRepository, PdfService pdfService, TicketRepository ticketRepository) {
        super(priceRepository, pdfService, ticketRepository);
    }


   @Override
    public List<String> createTickets(String email, List<Map<String, Object>> ticketDetails, LocalDateTime purchaseDate, LocalDateTime expirationDate) {
        return super.createTickets(email, ticketDetails, purchaseDate, expirationDate, 0, 1);
    }
}


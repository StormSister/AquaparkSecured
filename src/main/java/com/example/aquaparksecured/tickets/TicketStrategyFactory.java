package com.example.aquaparksecured.tickets;

import com.example.aquaparksecured.price.PriceRepository;
import org.springframework.stereotype.Component;

@Component
public class TicketStrategyFactory {
    private final PriceRepository priceRepository;
    private final PdfService pdfService;
    private final TicketRepository ticketRepository;

    public TicketStrategyFactory(PriceRepository priceRepository, PdfService pdfService, TicketRepository ticketRepository) {
        this.priceRepository = priceRepository;
        this.pdfService = pdfService;
        this.ticketRepository = ticketRepository;
    }

    public TicketStrategy getStrategy(String category) {
        switch (category) {
            case "Standard":
                return new StandardTicketStrategy(priceRepository, pdfService, ticketRepository);
            case "Senior":
                return new SeniorTicketStrategy(priceRepository, pdfService, ticketRepository);
            case "Group":
                return new GroupTicketStrategy(priceRepository, pdfService, ticketRepository);
            case "Excursion":
                return new ExcursionTicketStrategy(priceRepository, pdfService, ticketRepository);
            default:
                throw new IllegalArgumentException("Unknown ticket category: " + category);
        }
    }
}
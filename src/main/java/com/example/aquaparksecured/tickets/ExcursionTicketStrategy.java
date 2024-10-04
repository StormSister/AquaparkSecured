package com.example.aquaparksecured.tickets;

import com.example.aquaparksecured.price.PriceRepository;
import com.example.aquaparksecured.tickets.AbstractTicketStrategy;
import com.example.aquaparksecured.tickets.PdfService;
import com.example.aquaparksecured.tickets.Ticket;
import com.example.aquaparksecured.tickets.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ExcursionTicketStrategy extends AbstractTicketStrategy {

    public ExcursionTicketStrategy(PriceRepository priceRepository, PdfService pdfService, TicketRepository ticketRepository) {
        super(priceRepository, pdfService, ticketRepository);
    }

    @Override
    public List<String> createTickets(String email, List<Map<String, Object>> ticketDetails, LocalDateTime purchaseDate, LocalDateTime expirationDate) {
        List<String> pdfPaths = new ArrayList<>();

        for (Map<String, Object> detail : ticketDetails) {
            String category = (String) detail.get("category");
            int quantity = (int) detail.get("quantity");

            if (quantity < 20) {
                throw new IllegalArgumentException("Aby wybrać bilet typu Excursion, musi być co najmniej 20 osób.");
            }

            double excursionPrice = getPriceForCategory(category);

            for (int i = 0; i < quantity; i++) {
                Ticket ticket = createTicket(email, category, excursionPrice, purchaseDate, expirationDate, 1, 0);
                saveTicket(ticket);
                pdfPaths.add(ticket.getPdfPath());
            }
        }

        return pdfPaths;
    }
}

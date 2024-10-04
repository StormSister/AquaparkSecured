package com.example.aquaparksecured.tickets;

import com.example.aquaparksecured.price.PriceRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GroupTicketStrategy extends AbstractTicketStrategy {

    public GroupTicketStrategy(PriceRepository priceRepository, PdfService pdfService, TicketRepository ticketRepository) {
        super(priceRepository, pdfService, ticketRepository);
    }

    @Override
    public List<String> createTickets(String email, List<Map<String, Object>> ticketDetails, LocalDateTime purchaseDate, LocalDateTime expirationDate) {
        List<String> pdfPaths = new ArrayList<>();

        double totalPrice = calculateTotalPrice(ticketDetails);
        int[] totals = calculateTotalAdultsAndSeniors(ticketDetails);
        int totalAdults = totals[0];
        int totalSeniors = totals[1];


        Ticket groupTicket = createTicket(email, "Group", totalPrice, purchaseDate, expirationDate, totalAdults, totalSeniors);
        saveTicket(groupTicket);
        pdfPaths.add(groupTicket.getPdfPath());

        return pdfPaths;
    }


    private double calculateTotalPrice(List<Map<String, Object>> ticketDetails) {
        double totalPrice = 0.0;

        for (Map<String, Object> detail : ticketDetails) {
            String category = (String) detail.get("category");
            int quantity = (int) detail.get("quantity");

            double groupPrice = getPriceForCategory(category);
            totalPrice += groupPrice * quantity;
        }

        return totalPrice;
    }

    private int[] calculateTotalAdultsAndSeniors(List<Map<String, Object>> ticketDetails) {
        int totalAdults = 0;
        int totalSeniors = 0;

        for (Map<String, Object> detail : ticketDetails) {
            String category = (String) detail.get("category");
            int quantity = (int) detail.get("quantity");

            if ("Standard".equals(category)) {
                totalAdults += quantity;
            } else if ("Senior".equals(category)) {
                totalSeniors += quantity;
            }
        }

        return new int[] {totalAdults, totalSeniors};
    }
}

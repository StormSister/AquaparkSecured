package com.example.aquaparksecured.tickets;


import com.example.aquaparksecured.price.Price;
import com.example.aquaparksecured.price.PriceRepository;
import com.example.aquaparksecured.price.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final PriceService priceService;

    public TicketController(TicketService ticketService, PriceService priceService ) {
        this.ticketService = ticketService;
        this.priceService = priceService;
    }


    @PostMapping("/purchase")
    public ResponseEntity<String> purchaseTickets(@RequestBody Map<String, Object> requestData) {
        String email = (String) requestData.get("email");
        List<Map<String, Object>> ticketDetails = (List<Map<String, Object>>) requestData.get("ticketDetails");
        boolean isGroup = (boolean) requestData.get("isGroup");

        System.out.println("Received purchase request in controller:");
        System.out.println("Email: " + email);
        System.out.println("Ticket Details: " + ticketDetails);
        System.out.println("IsGroup: " + isGroup);

        try {
            ticketService.purchaseTickets(email, ticketDetails, isGroup);
            return ResponseEntity.ok("Tickets purchased successfully. They have been sent to your email.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Long id) {
        Ticket ticket = ticketService.findById(id);
        if (ticket != null) {
            return ResponseEntity.ok(ticket);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/tickets")
    public ResponseEntity<List<String>> getUserTicketPaths(@RequestParam("email") String userEmail) {
        System.out.println("Fetching ticket paths for user: " + userEmail);

        List<String> pdfPaths = ticketService.getUserTicketPath(userEmail);

        System.out.println("Found ticket paths: " + pdfPaths);

        return ResponseEntity.ok(pdfPaths);
    }

    @PostMapping("/api/check-qr")
    public ResponseEntity<String> checkTicket(@RequestBody Map<String, String> requestData) {
        String qrCodeText = requestData.get("qrCode");
        System.out.println("qrCodeText: " + qrCodeText);

        if (qrCodeText == null || qrCodeText.isEmpty()) {

            return ResponseEntity.badRequest().body("Invalid QR code.");
        }

        String status = ticketService.checkTicketStatus(qrCodeText);
        System.out.println("status :" + status);

        return ResponseEntity.ok(status);
    }

    @GetMapping("/ticket-types")
    public ResponseEntity<List<Map<String, Object>>> getTicketTypesAndPrices() {
        List<Price> ticketPrices = priceService.getPricesByType("Ticket");

        List<Map<String, Object>> ticketTypes = ticketPrices.stream()
                .map(price -> {
                    Map<String, Object> ticketInfo = new HashMap<>();
                    ticketInfo.put("type", price.getType());
                    ticketInfo.put("category", price.getCategory());
                    ticketInfo.put("price", price.getValue());
                    return ticketInfo;
                })
                .collect(Collectors.toList());
        System.out.println("Ticket types and prices: " + ticketTypes);
        return ResponseEntity.ok(ticketTypes);
    }
}

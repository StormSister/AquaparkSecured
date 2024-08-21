package com.example.aquaparksecured.tickets;


import com.example.aquaparksecured.price.Price;
import com.example.aquaparksecured.price.PriceService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final PriceService priceService;

    public TicketController(TicketService ticketService, PriceService priceService) {
        this.ticketService = ticketService;
        this.priceService = priceService;
    }

    private final Path ticketDirectory =  Paths.get("C:/Users/momika/AquaparkSecured/src/main/resources/tickets/");


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


    @GetMapping("/api/active")
    public List<String> getUserActiveTickets(@RequestParam("email") String userEmail) {
        System.out.println("Fetching active ticket names for user: " + userEmail);

        List<String> activeTicketNames = ticketService.getActiveUserTickets(userEmail);

        System.out.println("Found active ticket names: " + activeTicketNames);

        return activeTicketNames;
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

                    double standardPrice = price.getValue();
                    double finalPrice = ticketService.applyPromotionIfAvailable(standardPrice, price.getCategory());

                    ticketInfo.put("standardPrice", standardPrice);
                    ticketInfo.put("finalPrice", finalPrice);

                    ticketInfo.put("isPromotion", finalPrice < standardPrice);

                    return ticketInfo;
                })
                .collect(Collectors.toList());

        System.out.println("Ticket types and prices: " + ticketTypes);
        return ResponseEntity.ok(ticketTypes);
    }

    @GetMapping("/api/file/{filename:.+}")
    public ResponseEntity<Resource> getTicket(@PathVariable String filename) {
        System.out.println("Received request to download file: " + filename);

        try {
            Path filePath = ticketDirectory.resolve(filename).normalize();
            System.out.println("Resolved file path: " + filePath.toAbsolutePath());

            Resource resource = new UrlResource(filePath.toUri());
            System.out.println("Resource exists: " + resource.exists());
            System.out.println("Resource is readable: " + resource.isReadable());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                System.out.println("File not found or not readable: " + filePath.toAbsolutePath());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            System.out.println("Error occurred while retrieving the file: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

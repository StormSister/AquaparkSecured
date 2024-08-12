package com.example.aquaparksecured.tickets;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping("/purchase")
    public String purchaseTickets(@RequestBody Map<String, Object> requestData) {
        String email = (String) requestData.get("email");
        int adults = (int) requestData.get("adults");
        int children = (int) requestData.get("children");
        boolean isGroup = (boolean) requestData.get("isGroup");

        System.out.println("Received purchase request in controller:");
        System.out.println("Email: " + email);
        System.out.println("Adults: " + adults);
        System.out.println("Children: " + children);
        System.out.println("IsGroup: " + isGroup);

        ticketService.purchaseTickets(email, adults, children, isGroup);

        return "Tickets purchased successfully. They have been sent to your email.";
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
        System.out.println("status :" +status);

        return ResponseEntity.ok(status);
    }
}

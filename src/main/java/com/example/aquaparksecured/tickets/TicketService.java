package com.example.aquaparksecured.tickets;


import com.example.aquaparksecured.email.EmailService;
import com.example.aquaparksecured.price.PriceRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketService {

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private EmailService emailService;

    @Transactional
    public Ticket findById(Long id) {
        return ticketRepository.findById(id).orElse(null);
    }


    @Transactional
    public void purchaseTickets(String email, int adults, int children, boolean isGroup) {
        LocalDateTime purchaseDate = LocalDateTime.now();
        LocalDateTime expirationDate = purchaseDate.plusDays(31);

        System.out.println("Received purchase request:");
        System.out.println("Email: " + email);
        System.out.println("Adults: " + adults);
        System.out.println("Children: " + children);
        System.out.println("IsGroup: " + isGroup);

        List<String> pdfPaths;

        if (isGroup) {
            pdfPaths = createGroupTicket(email, adults, children, purchaseDate, expirationDate);
        } else {
            pdfPaths = createIndividualTickets(email, adults, children, purchaseDate, expirationDate);
        }

        // Send the email with the generated PDFs as attachments
        sendTicketsByEmail(email, pdfPaths);

        System.out.println("Tickets generated and email sent.");
    }

    private List<String> createGroupTicket(String email, int adults, int children, LocalDateTime purchaseDate, LocalDateTime expirationDate) {
        double price = calculateGroupPrice(adults, children);
        List<String> pdfPaths = new ArrayList<>();
        Ticket ticket = new Ticket();
        ticket.setEmail(email);
        ticket.setType("Group");
        ticket.setPrice(price);
        ticket.setPurchaseDate(purchaseDate);
        ticket.setExpirationDate(expirationDate);
        ticket.setQrCode(generateQrCode());
        ticket.setAdults(adults);
        ticket.setChildren(children);
        saveTicket(ticket);
        pdfPaths.add(ticket.getPdfPath());
        return pdfPaths;
    }

    private List<String> createIndividualTickets(String email, int adults, int children, LocalDateTime purchaseDate, LocalDateTime expirationDate) {
        double adultPrice = priceRepository.findByTypeAndCategory("Ticket", "Standard").getValue();
        double childPrice = priceRepository.findByTypeAndCategory("Ticket", "Child").getValue();
        List<String> pdfPaths = new ArrayList<>();

        for (int i = 0; i < adults; i++) {
            Ticket ticket = new Ticket();
            ticket.setEmail(email);
            ticket.setType("Standard");
            ticket.setPrice(adultPrice);
            ticket.setPurchaseDate(purchaseDate);
            ticket.setExpirationDate(expirationDate);
            ticket.setQrCode(generateQrCode());
            ticket.setAdults(1);
            ticket.setChildren(0);
            saveTicket(ticket);
            pdfPaths.add(ticket.getPdfPath());
        }

        for (int i = 0; i < children; i++) {
            Ticket ticket = new Ticket();
            ticket.setEmail(email);
            ticket.setType("Child");
            ticket.setPrice(childPrice);
            ticket.setPurchaseDate(purchaseDate);
            ticket.setExpirationDate(expirationDate);
            ticket.setQrCode(generateQrCode());
            ticket.setAdults(0);
            ticket.setChildren(1);
            saveTicket(ticket);
            pdfPaths.add(ticket.getPdfPath());
        }

        return pdfPaths;
    }

    private List<String> saveTicket(Ticket ticket) {
        ticket.setPdfPath("path/to/pdf");
        ticket.setStatus("active");

        System.out.println("Saving ticket:");
        System.out.println("Ticket ID: " + ticket.getId());
        System.out.println("Email: " + ticket.getEmail());
        System.out.println("Type: " + ticket.getType());
        System.out.println("Price: " + ticket.getPrice());
        System.out.println("Purchase Date: " + ticket.getPurchaseDate());
        System.out.println("Expiration Date: " + ticket.getExpirationDate());
        System.out.println("Adults: " + ticket.getAdults());
        System.out.println("Children: " + ticket.getChildren());
        System.out.println("QR Code: " + ticket.getQrCode());

        ticketRepository.save(ticket);

        try {
            String pdfPath = pdfService.generateTicketPdf(ticket);
            ticket.setPdfPath(pdfPath);
            ticketRepository.save(ticket);
            System.out.println("PDF generated successfully. Path: " + pdfPath);
//            return pdfPath;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    private void sendTicketsByEmail(String email, List<String> pdfPaths) {
        try {
            emailService.sendEmailWithAttachments(
                    email,
                    "Your Tickets",
                    "Thank you for your purchase! Please find your tickets attached.",
                    pdfPaths
            );
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    private double calculateGroupPrice(int adults, int children) {
        double adultPrice = priceRepository.findByTypeAndCategory("Ticket", "Standard").getValue();
        double childPrice = priceRepository.findByTypeAndCategory("Ticket", "Child").getValue();
        return (adults * adultPrice) + (children * childPrice);
    }


    private String generateQrCode() {
        return UUID.randomUUID().toString();
    }

    public List<Ticket> getUserTickets(Long userId) {
        return ticketRepository.findByUserId(userId);
    }

    public List<Ticket> getUserTickets(String email) {
        System.out.println("Fetching tickets for email: " + email);
        List<Ticket> tickets = ticketRepository.findByEmail(email);
        System.out.println("Found tickets: " + tickets.size());
        return tickets;
    }

    public List<String> getUserTicketPath(String email) {
        List<Ticket> tickets = ticketRepository.findByEmail(email);

        List<String> pdfPaths = tickets.stream()
                .map(Ticket::getPdfPath)
                .collect(Collectors.toList());

        return pdfPaths;
    }


    public String checkTicketStatus(String qrCodeText) {

        Ticket ticket = parseTicketFromQR(qrCodeText);

        if (ticket == null) {
            return "Invalid ticket format.";
        }

        Optional<Ticket> storedTicketOptional = ticketRepository.findById(ticket.getId());
        if (!storedTicketOptional.isPresent()) {
            return "Ticket not found.";
        }

        Ticket storedTicket = storedTicketOptional.get();

        if (isTicketExpired(storedTicket.getExpirationDate())) {
            return "Ticket expired.";
        }

        if ("used".equalsIgnoreCase(storedTicket.getStatus())) {
            return "Ticket already used.";
        } else if ("active".equalsIgnoreCase(storedTicket.getStatus())) {
            storedTicket.setStatus("used");
            ticketRepository.save(storedTicket);
            return "Ticket checked.";
        }

        return "Invalid ticket status.";
    }

    private boolean isTicketExpired(LocalDateTime expirationDate) {
        return expirationDate.isBefore(LocalDateTime.now());
    }

    private Ticket parseTicketFromQR(String qrCodeText) {
        String[] lines = qrCodeText.split("\n");
        Ticket ticket = new Ticket();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try {
            for (String line : lines) {
                if (line.startsWith("Ticket ID: ")) {
                    ticket.setId(Long.parseLong(line.substring(11).trim()));
                } else if (line.startsWith("Email: ")) {
                    ticket.setEmail(line.substring(7).trim());
                } else if (line.startsWith("Type: ")) {
                    ticket.setType(line.substring(6).trim());
                } else if (line.startsWith("Price: ")) {
                    ticket.setPrice(Double.parseDouble(line.substring(7).trim()));
                } else if (line.startsWith("Purchase Date: ")) {
                    String purchaseDateStr = line.substring(15).trim();
                    ticket.setPurchaseDate(LocalDateTime.parse(purchaseDateStr, formatter));
                } else if (line.startsWith("Expiration Date: ")) {
                    String expirationDateStr = line.substring(17).trim();
                    ticket.setExpirationDate(LocalDateTime.parse(expirationDateStr, formatter));
                } else if (line.startsWith("Adults: ")) {
                    ticket.setAdults(Integer.parseInt(line.substring(8).trim()));
                } else if (line.startsWith("Children: ")) {
                    ticket.setChildren(Integer.parseInt(line.substring(10).trim()));
                }
            }
        } catch (NumberFormatException | DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }

        return ticket;
    }

}
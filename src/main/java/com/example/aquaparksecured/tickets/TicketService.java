package com.example.aquaparksecured.tickets;

import com.example.aquaparksecured.email.EmailService;
import com.example.aquaparksecured.price.PriceRepository;
import com.example.aquaparksecured.promotion.Promotion;
import com.example.aquaparksecured.promotion.PromotionRepository;
import com.example.aquaparksecured.promotion.PromotionService;
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

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private PromotionService promotionService;



    @Transactional
    public Ticket findById(Long id) {
        return ticketRepository.findById(id).orElse(null);
    }

    @Transactional
    public void purchaseTickets(String email, List<Map<String, Object>> ticketDetails, boolean isGroup) {
        LocalDateTime purchaseDate = LocalDateTime.now();
        LocalDateTime expirationDate = purchaseDate.plusDays(31);

        Optional<Map<String, Object>> excursionDetail = ticketDetails.stream()
                .filter(detail -> "Excursion".equals(detail.get("category")))
                .findFirst();

        if (excursionDetail.isPresent()) {
            int excursionQuantity = (int) excursionDetail.get().get("quantity");
            if (excursionQuantity < 20) {
                throw new IllegalArgumentException("Aby wybrać bilet typu Excursion, musi być co najmniej 20 osób.");
            }
        }

        List<String> pdfPaths;

        if (isGroup) {
            pdfPaths = createGroupTicket(email, ticketDetails, purchaseDate, expirationDate);
        } else {
            pdfPaths = createIndividualTickets(email, ticketDetails, purchaseDate, expirationDate);
        }

        sendTicketsByEmail(email, pdfPaths);
    }

    private List<String> createGroupTicket(String email, List<Map<String, Object>> ticketDetails, LocalDateTime purchaseDate, LocalDateTime expirationDate) {
        double totalPrice = 0.0;
        List<String> pdfPaths = new ArrayList<>();
        Ticket ticket = new Ticket();
        ticket.setEmail(email);
        ticket.setType("Group");

        int totalAdults = 0;
        int totalSeniors = 0;

        for (Map<String, Object> detail : ticketDetails) {
            String category = (String) detail.get("category");
            int quantity = (int) detail.get("quantity");

            double standardPrice = priceRepository.findByTypeAndCategory("Ticket", category).getValue();
            double finalPrice = applyPromotionIfAvailable(standardPrice, category);

            totalPrice += finalPrice * quantity;

            if ("Standard".equals(category)) {
                totalAdults += quantity;
            } else if ("Senior".equals(category)) {
                totalSeniors += quantity;
            }
        }

        ticket.setPrice(totalPrice);
        ticket.setAdults(totalAdults);
        ticket.setSeniors(totalSeniors);
        ticket.setPurchaseDate(purchaseDate);
        ticket.setExpirationDate(expirationDate);
        ticket.setQrCode(generateQrCode());

        saveTicket(ticket);
        pdfPaths.add(ticket.getPdfPath());

        return pdfPaths;
    }

    private List<String> createIndividualTickets(String email, List<Map<String, Object>> ticketDetails, LocalDateTime purchaseDate, LocalDateTime expirationDate) {
        List<String> pdfPaths = new ArrayList<>();

        for (Map<String, Object> detail : ticketDetails) {
            String type = (String) detail.get("type");
            String category = (String) detail.get("category");
            int quantity = (int) detail.get("quantity");

            double standardPrice = priceRepository.findByTypeAndCategory(type, category).getValue();

            double finalPrice = applyPromotionIfAvailable(standardPrice, category);

            for (int i = 0; i < quantity; i++) {
                Ticket ticket = new Ticket();
                ticket.setEmail(email);
                ticket.setType(category);
                ticket.setPrice(finalPrice);
                ticket.setPurchaseDate(purchaseDate);
                ticket.setExpirationDate(expirationDate);
                ticket.setQrCode(generateQrCode());

                if ("Standard".equals(category)) {
                    ticket.setAdults(1);
                    ticket.setSeniors(0);
                } else if ("Senior".equals(category)) {
                    ticket.setAdults(0);
                    ticket.setSeniors(1);
                } else {
                    ticket.setAdults(0);
                    ticket.setSeniors(0);
                }

                saveTicket(ticket);
                pdfPaths.add(ticket.getPdfPath());
            }
        }

        return pdfPaths;
    }

    private double calculateGroupPrice(List<Map<String, Object>> ticketDetails) {
        double totalPrice = 0.0;

        // Handle excursion tickets separately
        Optional<Map<String, Object>> excursionDetail = ticketDetails.stream()
                .filter(detail -> "Excursion".equals(detail.get("category")))
                .findFirst();

        if (excursionDetail.isPresent()) {
            int quantity = (int) excursionDetail.get().get("quantity");
            double excursionPrice = priceRepository.findByTypeAndCategory("Ticket", "Excursion").getValue();
            totalPrice += quantity * excursionPrice;
        } else {
            // Handle non-excursion tickets
            for (Map<String, Object> detail : ticketDetails) {
                String category = (String) detail.get("category");
                int quantity = (int) detail.get("quantity");
                double ticketPrice = priceRepository.findByTypeAndCategory("Ticket", category).getValue();
                totalPrice += ticketPrice * quantity;
            }
        }

        return totalPrice;
    }

    private String generateQrCode() {
        return UUID.randomUUID().toString();
    }

    private void saveTicket(Ticket ticket) {
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
        System.out.println("Seniors: " + ticket.getSeniors());
        System.out.println("QR Code: " + ticket.getQrCode());

        ticketRepository.save(ticket);

        try {
            String pdfPath = pdfService.generateTicketPdf(ticket);
            ticket.setPdfPath(pdfPath);
            ticketRepository.save(ticket);
            System.out.println("PDF generated successfully. Path: " + pdfPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                } else if (line.startsWith("Seniors: ")) {
                    ticket.setSeniors(Integer.parseInt(line.substring(10).trim()));
                }
            }
        } catch (NumberFormatException | DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }

        return ticket;
    }

    public double applyPromotionIfAvailable(double originalPrice, String category) {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotions = promotionRepository.findActivePromotions(now);
        System.out.println("Promotions: " + promotions);

        double discountedPrice = originalPrice;
        for (Promotion promotion : promotions) {
            System.out.println("Checking promotion: " + promotion.getDescription());
            promotion.getCategories().forEach(pc -> System.out.println("Promotion category: " + pc.getCategory()));

            if (promotion.getCategories().stream().anyMatch(pc -> pc.getCategory().equalsIgnoreCase(category))) {
                double discount = promotion.getDiscountAmount();
                discountedPrice = originalPrice * (1 - discount / 100.0);
                System.out.println("Applied promotion: " + promotion.getDescription() + " with discount: " + discount);
                break;
            }
        }

        double finalPrice = Math.round(discountedPrice * 100.0) / 100.0;
        System.out.println("Final price after promotion: " + finalPrice);
        return finalPrice;
    }

}

package com.example.aquaparksecured.tickets;

import com.example.aquaparksecured.email.EmailService;
import com.example.aquaparksecured.promotion.Promotion;
import com.example.aquaparksecured.promotion.PromotionRepository;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TicketService {


    private final TicketRepository ticketRepository;
    private final PdfService pdfService;
    private final EmailService emailService;
    private final PromotionRepository promotionRepository;
    private final TicketStrategyFactory ticketStrategyFactory;

    private static final String TICKET_BASE_PATH = "tickets/";

    public TicketService(TicketRepository ticketRepository,
                         PdfService pdfService,
                         EmailService emailService,
                         PromotionRepository promotionRepository,
                         TicketStrategyFactory ticketStrategyFactory) {

        this.ticketRepository = ticketRepository;
        this.pdfService = pdfService;
        this.emailService = emailService;
        this.promotionRepository = promotionRepository;
        this.ticketStrategyFactory = ticketStrategyFactory;
    }

    @Transactional
    public Ticket findById(Long id) {
        return ticketRepository.findById(id).orElse(null);
    }

    @Transactional
    public void purchaseTickets(String email, List<Map<String, Object>> ticketDetails, boolean isGroup) {
        LocalDateTime purchaseDate = LocalDateTime.now();
        LocalDateTime expirationDate = purchaseDate.plusDays(31);

        validateExcursionRequirements(ticketDetails);

        List<String> pdfPaths;

        if (isGroup) {

            TicketStrategy groupTicketStrategy = ticketStrategyFactory.getStrategy("Group");
            pdfPaths = groupTicketStrategy.createTickets(email, ticketDetails, purchaseDate, expirationDate);
        } else {

            pdfPaths = ticketDetails.stream()
                    .map(detail -> (String) detail.get("category"))
                    .distinct()
                    .map(category -> ticketStrategyFactory.getStrategy(category))
                    .flatMap(strategy -> strategy.createTickets(email, ticketDetails, purchaseDate, expirationDate).stream())
                    .collect(Collectors.toList());
        }

        sendTicketsByEmail(email, pdfPaths);
    }

    private void validateExcursionRequirements(List<Map<String, Object>> ticketDetails) {
        Optional<Map<String, Object>> excursionDetail = ticketDetails.stream()
                .filter(detail -> "Excursion".equals(detail.get("category")))
                .findFirst();

        if (excursionDetail.isPresent()) {
            int excursionQuantity = (int) excursionDetail.get().get("quantity");
            if (excursionQuantity < 20) {
                throw new IllegalArgumentException("Aby wybrać bilet typu Excursion, musi być co najmniej 20 osób.");
            }
        }
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

    public List<String> getActiveUserTickets(String userEmail) {
        List<Ticket> activeTickets = ticketRepository.findByEmailAndStatus(userEmail, "active");

        return activeTickets.stream()
                .map(Ticket::extractFileName)
                .collect(Collectors.toList());
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
                    String lineValue = line.substring(8).trim();
                    if (lineValue.isEmpty()) {
                        System.out.println("Pusta wartość dla Adults.");
                        throw new NumberFormatException("Pusta wartość dla Adults.");
                    }
                    ticket.setAdults(Integer.parseInt(lineValue));
                } else if (line.startsWith("Seniors: ")) {
                    String lineValue = line.substring(9).trim();
                    if (lineValue.isEmpty()) {
                        System.out.println("Pusta wartość dla Seniors.");
                        throw new NumberFormatException("Pusta wartość dla Seniors.");
                    }
                    ticket.setSeniors(Integer.parseInt(lineValue));
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

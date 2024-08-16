package com.example.aquaparksecured.promotion;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addPromotion(
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr,
            @RequestParam("discountType") String discountType,
            @RequestParam("discountAmount") int discountAmount,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {

        System.out.println("Received startDate: " + startDateStr);
        System.out.println("Received endDate: " + endDateStr);
        System.out.println("Received discountType: " + discountType);
        System.out.println("Received discountAmount: " + discountAmount);
        System.out.println("Received description: " + description);

        if (imageFile != null) {
            System.out.println("Received image file: " + imageFile.getOriginalFilename());
            System.out.println("Image file size: " + imageFile.getSize() + " bytes");
        } else {
            System.out.println("No image file received.");
        }

        if (startDateStr == null || endDateStr == null || discountType == null || description == null) {
            return ResponseEntity.badRequest().body("Brakuje wymaganych pól.");
        }

        DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime startDate = LocalDateTime.parse(startDateStr, isoFormatter);
        LocalDateTime endDate = LocalDateTime.parse(endDateStr, isoFormatter);

        Timestamp sqlStartDate = Timestamp.valueOf(startDate.atZone(ZoneId.of("UTC")).toLocalDateTime());
        Timestamp sqlEndDate = Timestamp.valueOf(endDate.atZone(ZoneId.of("UTC")).toLocalDateTime());

        Promotion promotion = new Promotion();
        promotion.setStartDate(sqlStartDate);
        promotion.setEndDate(sqlEndDate);
        promotion.setDiscountType(discountType);
        promotion.setDiscountAmount(discountAmount);
        promotion.setDescription(description);

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String uniqueFilename = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                Path imagePath = Paths.get(uploadDir, uniqueFilename);

                if (!Files.exists(imagePath.getParent())) {
                    Files.createDirectories(imagePath.getParent());
                }

                Files.write(imagePath, imageFile.getBytes());

                promotion.setImagePath("/uploads/" + uniqueFilename);

                System.out.println("Image saved at: " + imagePath.toString());

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error while saving file: " + e.getMessage());
                return ResponseEntity.status(500).body("Błąd podczas zapisywania pliku.");
            }
        }

        promotionService.savePromotion(promotion);

        System.out.println("Promotion saved with ID: " + promotion.getId());
        System.out.println("Promotion details: " +
                "Start Date: " + promotion.getStartDate() +
                ", End Date: " + promotion.getEndDate() +
                ", Discount Type: " + promotion.getDiscountType() +
                ", Discount Amount: " + promotion.getDiscountAmount() +
                ", Description: " + promotion.getDescription() +
                ", Image Path: " + promotion.getImagePath());

        return ResponseEntity.ok("Promocja została dodana.");
    }

    @GetMapping("/current")
    public ResponseEntity<List<Promotion>> getCurrentPromotions() {
        LocalDateTime now = LocalDateTime.now();
        Timestamp currentTimestamp = Timestamp.valueOf(now);
        List<Promotion> currentPromotions = promotionService.getCurrentPromotions(currentTimestamp);
        System.out.println("Promotions " + currentPromotions);

        return ResponseEntity.ok(currentPromotions);
    }
}

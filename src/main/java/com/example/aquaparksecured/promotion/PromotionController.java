package com.example.aquaparksecured.promotion;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

        // Logowanie danych wejściowych
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

        // Walidacja danych wejściowych
        if (startDateStr == null || endDateStr == null || discountType == null || description == null) {
            return ResponseEntity.badRequest().body("Brakuje wymaganych pól.");
        }

        // Konwersja dat z formatu ISO 8601 do LocalDateTime
        DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime startDate = LocalDateTime.parse(startDateStr, isoFormatter);
        LocalDateTime endDate = LocalDateTime.parse(endDateStr, isoFormatter);

        // Konwersja LocalDateTime na Timestamp
        Timestamp sqlStartDate = Timestamp.valueOf(startDate.atZone(ZoneId.of("UTC")).toLocalDateTime());
        Timestamp sqlEndDate = Timestamp.valueOf(endDate.atZone(ZoneId.of("UTC")).toLocalDateTime());

        Promotion promotion = new Promotion();
        promotion.setStartDate(sqlStartDate);
        promotion.setEndDate(sqlEndDate);
        promotion.setDiscountType(discountType);
        promotion.setDiscountAmount(discountAmount);
        promotion.setDescription(description);

        // Obsługa pliku
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // Tworzenie unikalnej nazwy pliku, aby uniknąć nadpisywania
                String uniqueFilename = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                Path imagePath = Paths.get(uploadDir, uniqueFilename);

                // Tworzenie folderu, jeśli nie istnieje
                if (!Files.exists(imagePath.getParent())) {
                    Files.createDirectories(imagePath.getParent());
                }

                // Zapis pliku na dysk
                Files.write(imagePath, imageFile.getBytes());

                // Ustawienie ścieżki pliku w obiekcie promocji
                promotion.setImagePath("/uploads/" + uniqueFilename);

                // Logowanie ścieżki zapisanego pliku
                System.out.println("Image saved at: " + imagePath.toString());

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error while saving file: " + e.getMessage());
                return ResponseEntity.status(500).body("Błąd podczas zapisywania pliku.");
            }
        }

        // Zapis promocji
        promotionService.savePromotion(promotion);

        // Logowanie informacji o zapisanej promocji
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
}

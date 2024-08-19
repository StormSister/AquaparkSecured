package com.example.aquaparksecured.promotion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/promotions")
public class PromotionController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping("/api/add")
    public ResponseEntity<?> addPromotion(
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr,
            @RequestParam("categories") String categoriesJson,
            @RequestParam("discountAmount") int discountAmount,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {

        System.out.println("Received startDate: " + startDateStr);
        System.out.println("Received endDate: " + endDateStr);
        System.out.println("Received categories: " + categoriesJson);
        System.out.println("Received discountAmount: " + discountAmount);
        System.out.println("Received description: " + description);

        if (imageFile != null) {
            System.out.println("Received image file: " + imageFile.getOriginalFilename());
            System.out.println("Image file size: " + imageFile.getSize() + " bytes");
        } else {
            System.out.println("No image file received.");
        }

        if (startDateStr == null || endDateStr == null || categoriesJson == null || description == null) {
            return ResponseEntity.badRequest().body("Brakuje wymaganych pól.");
        }

        DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime startDate = LocalDateTime.parse(startDateStr, isoFormatter);
        LocalDateTime endDate = LocalDateTime.parse(endDateStr, isoFormatter);

        Timestamp sqlStartDate = Timestamp.valueOf(startDate);
        Timestamp sqlEndDate = Timestamp.valueOf(endDate);

        Promotion promotion = new Promotion();
        promotion.setStartDate(sqlStartDate);
        promotion.setEndDate(sqlEndDate);
        promotion.setDiscountAmount(discountAmount);
        promotion.setDescription(description);

        ObjectMapper objectMapper = new ObjectMapper();
        List<String> categories;
        try {
            categories = objectMapper.readValue(categoriesJson, new TypeReference<List<String>>() {});
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Błąd przy parsowaniu kategorii.");
        }

        List<PromotionCategory> promotionCategories = categories.stream()
                .map(categoryName -> {
                    PromotionCategory promotionCategory = new PromotionCategory();
                    promotionCategory.setCategory(categoryName.trim());
                    promotionCategory.setPromotion(promotion);
                    return promotionCategory;
                })
                .collect(Collectors.toList());

        promotion.setCategories(promotionCategories);

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
                ", Categories: " + promotion.getCategories().stream().map(PromotionCategory::getCategory).collect(Collectors.toList()) +
                ", Discount Amount: " + promotion.getDiscountAmount() +
                ", Description: " + promotion.getDescription() +
                ", Image Path: " + promotion.getImagePath());

        return ResponseEntity.ok("Promocja została dodana.");
    }


    private PromotionDTO convertToDTO(Promotion promotion) {
        PromotionDTO dto = new PromotionDTO();
        dto.setId(promotion.getId());
        dto.setStartDate(promotion.getStartDate());
        dto.setEndDate(promotion.getEndDate());
        dto.setDiscountAmount(promotion.getDiscountAmount());
        dto.setDescription(promotion.getDescription());
        dto.setImagePath(promotion.getImagePath());

        List<CategoryDTO> categoryDTOs = promotion.getCategories().stream()
                .map(this::convertCategoryToDTO)
                .collect(Collectors.toList());
        dto.setCategories(categoryDTOs);

        return dto;
    }

    private CategoryDTO convertCategoryToDTO(PromotionCategory category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setCategory(category.getCategory());
        return dto;
    }


    @GetMapping("/current")
    public ResponseEntity<List<PromotionDTO>> getCurrentPromotions() {
        LocalDateTime now = LocalDateTime.now();
        Timestamp currentTimestamp = Timestamp.valueOf(now);
        List<Promotion> promotions = promotionService.getCurrentPromotions(currentTimestamp);
        System.out.println("Promotions: " + promotions);

        // Map to DTOs
        List<PromotionDTO> promotionDTOs = promotions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(promotionDTOs);
    }


}

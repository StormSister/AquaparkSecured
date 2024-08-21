package com.example.aquaparksecured.promotion;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
            @RequestParam("startDisplay") String startDisplayStr,
            @RequestParam("endDisplay") String endDisplayStr,
            @RequestParam("categories") String categoriesJson,
            @RequestParam("discountAmount") int discountAmount,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            HttpServletRequest request) {

        // Log all request parameters
        System.out.println("Request Parameters:");
        System.out.println("startDate: " + startDateStr);
        System.out.println("endDate: " + endDateStr);
        System.out.println("startDisplay: " + startDisplayStr);
        System.out.println("endDisplay: " + endDisplayStr);
        System.out.println("categories: " + categoriesJson);
        System.out.println("discountAmount: " + discountAmount);
        System.out.println("description: " + description);

        // Log headers for debugging
        System.out.println("Request Headers:");
        request.getHeaderNames().asIterator().forEachRemaining(headerName ->
                System.out.println(headerName + ": " + request.getHeader(headerName))
        );

        if (startDateStr == null || endDateStr == null || startDisplayStr == null || endDisplayStr == null ||
                categoriesJson == null || description == null) {
            return ResponseEntity.badRequest().body("Missing required fields.");
        }

        DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime startDate = LocalDateTime.parse(startDateStr, isoFormatter);
        LocalDateTime endDate = LocalDateTime.parse(endDateStr, isoFormatter);
        LocalDateTime startDisplay = LocalDateTime.parse(startDisplayStr, isoFormatter);
        LocalDateTime endDisplay = LocalDateTime.parse(endDisplayStr, isoFormatter);

        // Log parsed dates
        System.out.println("Parsed dates:");
        System.out.println("startDate: " + startDate);
        System.out.println("endDate: " + endDate);
        System.out.println("startDisplay: " + startDisplay);
        System.out.println("endDisplay: " + endDisplay);

        Timestamp sqlStartDate = Timestamp.valueOf(startDate);
        Timestamp sqlEndDate = Timestamp.valueOf(endDate);
        Timestamp sqlStartDisplay = Timestamp.valueOf(startDisplay);
        Timestamp sqlEndDisplay = Timestamp.valueOf(endDisplay);

        Promotion promotion = new Promotion();
        promotion.setStartDate(sqlStartDate);
        promotion.setEndDate(sqlEndDate);
        promotion.setStartDisplay(sqlStartDisplay);
        promotion.setEndDisplay(sqlEndDisplay);
        promotion.setDiscountAmount(discountAmount);
        promotion.setDescription(description);

        // Log Promotion object
        System.out.println("Promotion object:");
        System.out.println(promotion);

        ObjectMapper objectMapper = new ObjectMapper();
        List<String> categories;
        try {
            categories = objectMapper.readValue(categoriesJson, new TypeReference<List<String>>() {});
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error parsing categories.");
        }

        // Log parsed categories
        System.out.println("Parsed categories:");
        categories.forEach(System.out::println);

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

                // Log image file details
                System.out.println("Image file saved:");
                System.out.println("Filename: " + uniqueFilename);
                System.out.println("Path: " + imagePath);
            } catch (IOException e) {
                return ResponseEntity.status(500).body("Error saving file.");
            }
        }

        promotionService.savePromotion(promotion);

        return ResponseEntity.ok("Promotion added.");
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
        System.out.println("Current promotions:");
        promotions.forEach(System.out::println);

        // Map to DTOs
        List<PromotionDTO> promotionDTOs = promotions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(promotionDTOs);
    }

    @GetMapping("/currentDisplay")
    public ResponseEntity<List<PromotionDTO>> getCurrentDisplayPromotions() {
        LocalDateTime now = LocalDateTime.now();
        Timestamp currentTimestamp = Timestamp.valueOf(now);
        List<Promotion> promotions = promotionService.getPromotionsForDisplayDateRange(currentTimestamp);
        System.out.println("Display promotions:");
        promotions.forEach(System.out::println);

        List<PromotionDTO> promotionDTOs = promotions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(promotionDTOs);
    }
}

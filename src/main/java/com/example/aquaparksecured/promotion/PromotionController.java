package com.example.aquaparksecured.promotion;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    @Autowired
    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping("/api/add")
    public ResponseEntity<String> addPromotion(
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr,
            @RequestParam("discountType") String discountType,
            @RequestParam("discountAmount") int discountAmount,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);

            promotionService.addPromotion(startDate, endDate, discountType, discountAmount, description, image);
            return ResponseEntity.ok("Promocja została dodana!");
        } catch (ParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nieprawidłowy format daty!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wystąpił błąd podczas dodawania promocji!");
        }
    }

    @GetMapping("/current")
    public ResponseEntity<List<Promotion>> getCurrentPromotions() {
        List<Promotion> promotions = promotionService.getCurrentPromotions();
        return ResponseEntity.ok(promotions);
    }

    @GetMapping(value = "/image/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getPromotionImage(@PathVariable Long id) throws IOException {
        byte[] image = promotionService.getPromotionImage(id);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
    }

    @GetMapping("/api/search")
    public ResponseEntity<List<Promotion>> searchPromotions(
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(name = "type", required = false) String discountType
    ) {
        List<Promotion> promotions = promotionService.searchPromotions(startDate, endDate, discountType);
        return ResponseEntity.ok(promotions);
    }
}

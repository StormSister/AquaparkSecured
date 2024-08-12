package com.example.aquaparksecured.promotion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;

    @Autowired
    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    public void addPromotion(Date startDate, Date endDate, String discountType, int discountAmount, String description, MultipartFile image) throws IOException {
        Promotion promotion = new Promotion();
        promotion.setStartDate(startDate);
        promotion.setEndDate(endDate);
        promotion.setDiscountType(discountType);
        promotion.setDiscountAmount(discountAmount);
        promotion.setDescription(description);

        if (image != null && !image.isEmpty()) {
            promotion.setImage(image.getBytes());
        }

        promotionRepository.save(promotion);
    }

    public List<Promotion> getCurrentPromotions() {
        Date currentDate = new Date();
        return promotionRepository.findCurrentPromotions(currentDate);
    }

    public byte[] getPromotionImage(Long id) throws IOException {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono promocji o podanym id: " + id));
        return promotion.getImage();
    }

    public List<Promotion> searchPromotions(Date startDate, Date endDate, String discountType) {
        return promotionRepository.findByCriteria(startDate, endDate, discountType);
    }
}




package com.example.aquaparksecured.promotion;


import org.springframework.stereotype.Service;


import java.sql.Timestamp;
import java.util.List;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    public Promotion savePromotion(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    public List<Promotion> getCurrentPromotions(Timestamp currentTimestamp) {
        return promotionRepository.findByStartDateBeforeAndEndDateAfter(currentTimestamp, currentTimestamp);
    }
}
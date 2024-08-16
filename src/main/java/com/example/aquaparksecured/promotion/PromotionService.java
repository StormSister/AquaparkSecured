package com.example.aquaparksecured.promotion;


import org.springframework.stereotype.Service;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    public Promotion savePromotion(Promotion promotion) {
        return promotionRepository.save(promotion);
    }
}
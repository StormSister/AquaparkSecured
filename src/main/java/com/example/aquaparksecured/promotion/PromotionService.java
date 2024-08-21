package com.example.aquaparksecured.promotion;


import org.springframework.stereotype.Service;


import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public double applyPromotionIfAvailable(double originalPrice, String category) {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotions = promotionRepository.findActivePromotions(now);

        double discountedPrice = originalPrice;
        for (Promotion promotion : promotions) {
            if (promotion.getCategories().stream().anyMatch(pc -> pc.getCategory().equalsIgnoreCase(category))) {
                double discount = promotion.getDiscountAmount();
                discountedPrice = originalPrice * (1 - discount / 100.0);
                break;
            }
        }

        return Math.round(discountedPrice * 100.0) / 100.0;
    }

    public List<Promotion> getPromotionsForDateRange(LocalDate startDate, LocalDate endDate) {
        Timestamp startTimestamp = Timestamp.valueOf(startDate.atStartOfDay());
        Timestamp endTimestamp = Timestamp.valueOf(endDate.atStartOfDay());
        return promotionRepository.findPromotionsForDateRange(startTimestamp, endTimestamp);
    }

    public List<Promotion> getPromotionsForDisplayDateRange(Timestamp now) {
        return promotionRepository.findByStartDisplayBeforeAndEndDisplayAfter(now, now);
    }
}
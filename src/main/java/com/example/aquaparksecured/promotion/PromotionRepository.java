package com.example.aquaparksecured.promotion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;


public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Query("SELECT p FROM Promotion p WHERE :currentDate BETWEEN p.startDate AND p.endDate")
    List<Promotion> findCurrentPromotions(@Param("currentDate") Date currentDate);

    @Query("SELECT p FROM Promotion p " +
            "WHERE (:startDate IS NULL OR p.startDate >= :startDate) " +
            "AND (:endDate IS NULL OR p.endDate <= :endDate) " +
            "AND (:discountType IS NULL OR p.discountType = :discountType)")
    List<Promotion> findByCriteria(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("discountType") String discountType
    );

    List<Promotion> findByStartDateBeforeAndEndDateAfter(Timestamp startDate, Timestamp endDate);

    @Query("SELECT p FROM Promotion p WHERE p.startDate <= :now AND p.endDate >= :now AND p.discountType = :category")
    Optional<Promotion> findActivePromotionForCategory(@Param("now") LocalDateTime now, @Param("category") String category);

}

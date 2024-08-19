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

//    @Query("SELECT p FROM Promotion p " +
//            "WHERE (:startDate IS NULL OR p.startDate >= :startDate) " +
//            "AND (:endDate IS NULL OR p.endDate <= :endDate) " +
//            "AND (:discountType IS NULL OR p.discountType = :discountType)")
//    List<Promotion> findByCriteria(
//            @Param("startDate") Date startDate,
//            @Param("endDate") Date endDate,
//            @Param("discountType") String discountType
//    );

    @Query("SELECT DISTINCT p FROM Promotion p JOIN FETCH p.categories c WHERE :now BETWEEN p.startDate AND p.endDate")
    List<Promotion> findActivePromotions(LocalDateTime now);

    List<Promotion> findByStartDateBeforeAndEndDateAfter(Timestamp startDate, Timestamp endDate);

    @Query("SELECT p FROM Promotion p JOIN p.categories c WHERE p.startDate <= :now AND p.endDate >= :now AND c.category = :category")
    Optional<Promotion> findActivePromotionForCategory(@Param("now") LocalDateTime now, @Param("category") String category);


}

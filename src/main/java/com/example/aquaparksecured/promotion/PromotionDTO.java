package com.example.aquaparksecured.promotion;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
public class PromotionDTO {

    private Long id;
    private Timestamp startDate;
    private Timestamp endDate;
    private int discountAmount;
    private String description;
    private String imagePath;
    private List<CategoryDTO> categories;

}
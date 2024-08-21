package com.example.aquaparksecured.promotion;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "promotion")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date")
    private Timestamp startDate;

    @Column(name = "end_date")
    private Timestamp endDate;

    @Column(name = "start_display")
    private Timestamp startDisplay;

    @Column(name = "end_display")
    private Timestamp endDisplay;

    @Column(name = "discount_amount")
    private int discountAmount;

    @Column(name = "description")
    private String description;

    @Column(name = "image_path")
    private String imagePath;

    // One-to-Many relationship with PromotionCategory
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JsonManagedReference
    private List<PromotionCategory> categories;
}

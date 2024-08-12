package com.example.aquaparksecured.promotion;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "promotion")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "discount_type")
    private String discountType;

    @Column(name = "discount_amount")
    private int discountAmount;

    @Column(name = "description")
    private String description;

    @Lob
    @Column(name = "image", columnDefinition = "bytea")
    private byte[] image;

}

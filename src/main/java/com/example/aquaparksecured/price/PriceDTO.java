package com.example.aquaparksecured.price;


import lombok.Data;

@Data
public class PriceDTO {
    private Long id;
    private String type;
    private String category;
    private double price;
}

package com.example.aquaparksecured.room;


import lombok.Data;



import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RoomTypeResponse {
    private String name;
    private int capacity;
    private int beds;
    private String description;
    private double price;
    private double finalPrice;
    private boolean isPromotion;
    private int availableCount;
    private String imagePath;
}
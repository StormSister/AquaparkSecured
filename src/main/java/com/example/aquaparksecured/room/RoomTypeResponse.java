package com.example.aquaparksecured.room;


import lombok.Data;



import lombok.Data;
@Data
public class RoomTypeResponse {
    private String name;
    private int capacity;
    private int beds;
    private String description;
    private double price;
    private int availableCount;
    private String imagePath;
}

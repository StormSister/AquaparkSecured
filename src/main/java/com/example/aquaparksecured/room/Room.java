package com.example.aquaparksecured.room;


import com.example.aquaparksecured.price.Price;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private int capacity;
    private int beds;
    private String description;
    private String imagePath;

    @ManyToOne
    @JoinColumn(name = "price_id", nullable = false)
    private Price price;
}
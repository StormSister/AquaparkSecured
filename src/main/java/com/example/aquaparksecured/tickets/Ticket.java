package com.example.aquaparksecured.tickets;

import com.example.aquaparksecured.user.AppUser;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tickets")
@Getter
@Setter
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private double price;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    @Column(name = "qr_code", nullable = false, columnDefinition = "TEXT")
    private String qrCode;

    @Column(nullable = false)
    private int adults;

    @Column(nullable = false)
    private int seniors;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String pdfPath;

    @Column(nullable = false)
    private String status;
}
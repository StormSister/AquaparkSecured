package com.example.aquaparksecured.reservation;

import java.time.LocalDate;

import com.example.aquaparksecured.room.Room;
import com.example.aquaparksecured.user.AppUser;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    private LocalDate startDate;
    private LocalDate endDate;
}

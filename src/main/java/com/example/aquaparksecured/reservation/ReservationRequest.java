package com.example.aquaparksecured.reservation;


import com.example.aquaparksecured.user.AppUser;

import lombok.Data;

import java.time.LocalDate;


@Data
public class ReservationRequest {
    private String roomType;
    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfPersons;
    private AppUser user;
}

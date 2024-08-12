package com.example.aquaparksecured.reservation;



import lombok.Data;

import java.time.LocalDate;
@Data
public class ReservationResponseDTO {
    private Long id;
    private String userEmail;
    private String userName;
    private String phoneNumber;
    private String roomType;
    private LocalDate startDate;
    private LocalDate endDate;

}
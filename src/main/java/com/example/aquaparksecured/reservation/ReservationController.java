package com.example.aquaparksecured.reservation;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reservations")
@CrossOrigin(origins = "http://localhost:3000")
public class ReservationController {

    private final ReservationService reservationService;

    @Autowired
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<?> makeReservation(@RequestBody List<ReservationRequest> reservationRequests) {
        try {
            System.out.println("Received reservation requests: " + reservationRequests);
            reservationService.makeReservation(reservationRequests);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the reservation.");
        }
    }

//    @GetMapping("/user")
//    public ResponseEntity<List<Reservation>> getUserReservationsByEmail(@RequestParam String email) {
//        try {
//            List<Reservation> reservations = reservationService.getUserReservationsByEmail(email);
//            return ResponseEntity.ok(reservations);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

    @DeleteMapping("/api/{id}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        try {
            System.out.println("Received request to cancel reservation with ID: " + id);
            reservationService.cancelReservation(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while cancelling the reservation.");
        }
    }

    @PutMapping("/api/{id}")
    public ResponseEntity<?> updateReservation(@PathVariable Long id, @RequestBody ReservationRequest reservationRequest) {
        try {
            reservationService.updateReservation(id, reservationRequest);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the reservation.");
        }
    }

    @GetMapping("/api/user")
    public ResponseEntity<List<ReservationResponseDTO>> getUserReservationsByEmail(@RequestParam String email) {
        try {
            List<Reservation> reservations = reservationService.getUserReservationsByEmail(email);
            List<ReservationResponseDTO> reservationDTOs = reservations.stream().map(this::convertToDTO).collect(Collectors.toList());
            return ResponseEntity.ok(reservationDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/search")
    public ResponseEntity<List<ReservationResponseDTO>> searchReservations(@RequestParam String startDate, @RequestParam String endDate) {
        try {
            List<Reservation> reservations = reservationService.searchReservations(startDate, endDate);
            List<ReservationResponseDTO> reservationDTOs = reservations.stream().map(this::convertToDTO).collect(Collectors.toList());
            return ResponseEntity.ok(reservationDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/all")
    public ResponseEntity<List<ReservationResponseDTO>> getAllReservations() {
        try {
            List<Reservation> reservations = reservationService.getAllReservations();
            List<ReservationResponseDTO> reservationDTOs = reservations.stream().map(this::convertToDTO).collect(Collectors.toList());
            return ResponseEntity.ok(reservationDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ReservationResponseDTO convertToDTO(Reservation reservation) {
        ReservationResponseDTO dto = new ReservationResponseDTO();
        dto.setId(reservation.getId());
        dto.setUserEmail(reservation.getUser().getEmail());
        dto.setUserName(reservation.getUser().getFirstName() + " " + reservation.getUser().getLastName());
        dto.setPhoneNumber(reservation.getUser().getPhoneNumber());
        dto.setRoomType(reservation.getRoom().getType());
        dto.setStartDate(reservation.getStartDate());
        dto.setEndDate(reservation.getEndDate());
        System.out.println(dto);
        return dto;
    }


}

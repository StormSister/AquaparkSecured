package com.example.aquaparksecured.room;

import com.example.aquaparksecured.reservation.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;





@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;
    private final ReservationService reservationService;

    @Autowired
    public RoomController(RoomService roomService, ReservationService reservationService) {
        this.roomService = roomService;
        this.reservationService = reservationService;
    }

    @GetMapping("/available")
    public ResponseEntity<List<RoomTypeResponse>> getAvailableRoomTypes(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        List<RoomTypeResponse> availableRoomTypes = roomService.getAvailableRoomTypes(startDate, endDate);

        availableRoomTypes.forEach(roomType -> {
            double standardPrice = roomType.getPrice();
            double finalPrice = reservationService.applyRoomPromotionIfAvailable(standardPrice, roomType.getName(), startDate, endDate);
            roomType.setFinalPrice(finalPrice);
            roomType.setPromotion(finalPrice < standardPrice);
        });

        return ResponseEntity.ok(availableRoomTypes);
    }

}

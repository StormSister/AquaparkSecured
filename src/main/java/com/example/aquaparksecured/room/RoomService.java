package com.example.aquaparksecured.room;

import com.example.aquaparksecured.promotion.Promotion;
import com.example.aquaparksecured.promotion.PromotionService;
import com.example.aquaparksecured.room.Room;
import com.example.aquaparksecured.room.RoomRepository;
import com.example.aquaparksecured.room.RoomTypeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final PromotionService promotionService;

    @Autowired
    public RoomService(RoomRepository roomRepository, PromotionService promotionService) {
        this.roomRepository = roomRepository;
        this.promotionService = promotionService;
    }

    public List<RoomTypeResponse> getAvailableRoomTypes(LocalDate startDate, LocalDate endDate) {
        List<Room> availableRooms = roomRepository.findAvailableRooms(startDate, endDate);

        Map<String, List<Room>> roomsByType = availableRooms.stream()
                .collect(Collectors.groupingBy(Room::getType));

        return roomsByType.entrySet().stream()
                .map(entry -> {
                    String type = entry.getKey();
                    List<Room> rooms = entry.getValue();
                    int availableCount = roomRepository.countAvailableRoomsByType(type, startDate, endDate);
                    RoomTypeResponse response = convertToRoomTypeResponse(rooms.get(0));
                    response.setAvailableCount(availableCount);

                    double standardPrice = response.getPrice();
                    double finalPrice = applyRoomPromotionIfAvailable(standardPrice, type, startDate, endDate);
                    response.setFinalPrice(finalPrice);
                    response.setPromotion(finalPrice < standardPrice);

                    return response;
                })
                .collect(Collectors.toList());
    }

    private double applyRoomPromotionIfAvailable(double originalPrice, String category, LocalDate startDate, LocalDate endDate) {
        List<Promotion> promotions = promotionService.getPromotionsForDateRange(startDate, endDate);

        double discountedPrice = originalPrice;
        for (Promotion promotion : promotions) {
            if (promotion.getCategories().stream().anyMatch(pc -> pc.getCategory().equalsIgnoreCase(category))) {
                double discount = promotion.getDiscountAmount();
                discountedPrice = originalPrice * (1 - discount / 100.0);
                break;
            }
        }

        return Math.round(discountedPrice * 100.0) / 100.0;
    }

    private RoomTypeResponse convertToRoomTypeResponse(Room room) {
        RoomTypeResponse response = new RoomTypeResponse();
        response.setName(room.getType());
        response.setCapacity(room.getCapacity());
        response.setBeds(room.getBeds());
        response.setDescription(room.getDescription());
        response.setPrice(room.getPrice().getValue());
        response.setFinalPrice(room.getPrice().getValue());
        response.setImagePath(room.getImagePath());
        return response;
    }
}
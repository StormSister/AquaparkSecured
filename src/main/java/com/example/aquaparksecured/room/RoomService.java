package com.example.aquaparksecured.room;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;


import java.util.stream.Collectors;
import java.util.Map;


@Service
public class RoomService {

    private final RoomRepository roomRepository;

    @Autowired
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
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
                    System.out.println(response);
                    return response;
                })
                .collect(Collectors.toList());
    }

    private RoomTypeResponse convertToRoomTypeResponse(Room room) {
        RoomTypeResponse response = new RoomTypeResponse();
        response.setName(room.getType());
        response.setCapacity(room.getCapacity());
        response.setBeds(room.getBeds());
        response.setDescription(room.getDescription());
        response.setPrice(room.getPrice().getValue());
        response.setImagePath(room.getImagePath());
        return response;
    }
}

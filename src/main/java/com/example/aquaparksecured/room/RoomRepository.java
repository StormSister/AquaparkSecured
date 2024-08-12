package com.example.aquaparksecured.room;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r WHERE r.id NOT IN " +
            "(SELECT res.room.id FROM Reservation res WHERE :startDate <= res.endDate AND :endDate >= res.startDate)")
    List<Room> findAvailableRooms(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.type = :type " +
            "AND r.id NOT IN " +
            "(SELECT res.room.id FROM Reservation res WHERE :startDate <= res.endDate AND :endDate >= res.startDate)")
    int countAvailableRoomsByType(@Param("type") String type,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    @Query("SELECT r FROM Room r WHERE r.type = :type AND r.id NOT IN " +
            "(SELECT res.room.id FROM Reservation res WHERE :startDate <= res.endDate AND :endDate >= res.startDate)")
    List<Room> findAvailableRoomsByType(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        @Param("type") String type);
}
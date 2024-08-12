package com.example.aquaparksecured.reservation;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserEmail(String email);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.room JOIN FETCH r.user WHERE (r.startDate <= :endDate AND r.endDate >= :startDate)")
    List<Reservation> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.room JOIN FETCH r.user")
    List<Reservation> findAllWithDetails();
}

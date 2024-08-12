package com.example.aquaparksecured.price;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {
    List<Price> findByType(String type);

    Price findByTypeAndCategory(String type, String category);


    List<Price> findByCategory(String category);

    List<Price> findByTypeOrCategory(String type, String category);


}
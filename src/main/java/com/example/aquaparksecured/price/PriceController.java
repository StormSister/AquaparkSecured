package com.example.aquaparksecured.price;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/prices")

public class PriceController {

    private final PriceService priceService;

    @Autowired
    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping
    public ResponseEntity<List<Price>> getAllPrices() {
        List<Price> prices = priceService.getAllPrices();
        return ResponseEntity.ok(prices);
    }


    @PostMapping("/api/add")
    public ResponseEntity<PriceDTO> addPrice(@RequestBody PriceDTO priceDTO) {
        Price price = convertToEntity(priceDTO);
        Price savedPrice = priceService.addPrice(price);
        PriceDTO savedPriceDTO = convertToDTO(savedPrice);
        return ResponseEntity.ok(savedPriceDTO);
    }

    @PutMapping("/api/update/{id}")
    public ResponseEntity<PriceDTO> updatePrice(@PathVariable Long id, @RequestBody PriceDTO priceDTO) {
        Price price = convertToEntity(priceDTO);
        Price updatedPrice = priceService.updatePrice(id, price);
        PriceDTO updatedPriceDTO = convertToDTO(updatedPrice);
        return ResponseEntity.ok(updatedPriceDTO);
    }

    @DeleteMapping("/api/delete/{id}")
    public ResponseEntity<Void> deletePrice(@PathVariable Long id) {
        priceService.deletePrice(id);
        return ResponseEntity.noContent().build();
    }

    private PriceDTO convertToDTO(Price price) {
        PriceDTO priceDTO = new PriceDTO();
        priceDTO.setId(price.getId());
        priceDTO.setType(price.getType());
        priceDTO.setCategory(price.getCategory());
        priceDTO.setPrice(price.getPrice());
        return priceDTO;
    }

    private Price convertToEntity(PriceDTO priceDTO) {
        Price price = new Price();
        price.setId(priceDTO.getId());
        price.setType(priceDTO.getType());
        price.setCategory(priceDTO.getCategory());
        price.setPrice(priceDTO.getPrice());
        return price;
    }
}
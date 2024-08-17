package com.example.aquaparksecured.price;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriceService {

    @Autowired
    private PriceRepository priceRepository;

    public List<Price> findByType(String type) {
        return priceRepository.findByType(type);
    }

    public List<Price> findByCategory(String category) {
        return priceRepository.findByCategory(category);
    }

    public Price findByTypeAndCategory(String type, String category) {
        return priceRepository.findByTypeAndCategory(type, category);
    }

    public List<Price> findByTypeOrCategory(String type, String category) {
        return priceRepository.findByTypeOrCategory(type, category);
    }

    public Price addPrice(Price price) {

        return priceRepository.save(price);
    }

    public Price updatePrice(Long id, Price updatedPrice) {
        Price existingPrice = priceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Price not found with id: " + id));

        existingPrice.setType(updatedPrice.getType());
        existingPrice.setCategory(updatedPrice.getCategory());
        existingPrice.setPrice(updatedPrice.getPrice());

        return priceRepository.save(existingPrice);
    }

    public void deletePrice(Long id) {
        priceRepository.deleteById(id);
    }

    public List<Price> getAllPrices() {
        return priceRepository.findAll();
    }

    public List<Price> getPricesByType(String type) {
        return priceRepository.findByType(type);
    }
}
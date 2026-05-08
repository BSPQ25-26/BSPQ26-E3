package com.example.restapi.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.restapi.dto.SaleResponse;
import com.example.restapi.service.SaleService;

@RestController
@RequestMapping("/api/sales")
public class SalesController {

    private final SaleService saleService;

    public SalesController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<SaleResponse>> getSalesBySellerId(@PathVariable String sellerId) {
        try {
            UUID uuid = UUID.fromString(sellerId);
            List<SaleResponse> sales = saleService.getSalesBySellerId(uuid);
            return ResponseEntity.ok(sales);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/seller/{sellerId}/total")
    public ResponseEntity<Map<String, Object>> getTotalSalesBySellerId(@PathVariable String sellerId) {
        try {
            UUID uuid = UUID.fromString(sellerId);
            Double totalSales = saleService.getTotalSalesBySellerId(uuid);
            List<SaleResponse> sales = saleService.getSalesBySellerId(uuid);
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalSales", totalSales);
            response.put("numberOfSales", sales.size());
            response.put("sales", sales);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

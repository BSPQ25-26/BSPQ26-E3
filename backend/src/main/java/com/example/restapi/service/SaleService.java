package com.example.restapi.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.restapi.dto.SaleResponse;
import com.example.restapi.model.Cart;
import com.example.restapi.model.CartItem;
import com.example.restapi.model.Item;
import com.example.restapi.model.Receipt;
import com.example.restapi.model.Sale;
import com.example.restapi.repository.SaleRepository;

@Service
public class SaleService {

    private final SaleRepository saleRepository;

    public SaleService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @Transactional
    public void recordSalesFromCheckout(Receipt receipt, Cart cart) {
        for (CartItem ci : cart.getItems()) {
            Item item = ci.getItem();
            UUID sellerId = item.getSeller().getId();

            Sale sale = new Sale(sellerId, receipt, item, ci.getQuantity(), item.getAmount());
            saleRepository.save(sale);
        }
    }

    public List<SaleResponse> getSalesBySellerId(UUID sellerId) {
        List<Sale> sales = saleRepository.findBySellerId(sellerId);
        return sales.stream()
                .map(sale -> new SaleResponse(
                        sale.getId(),
                        sale.getItem().getName(),
                        sale.getQuantity(),
                        sale.getUnitPrice(),
                        sale.getTotalPrice(),
                        sale.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public Double getTotalSalesBySellerId(UUID sellerId) {
        List<Sale> sales = saleRepository.findBySellerId(sellerId);
        return sales.stream()
                .mapToDouble(Sale::getTotalPrice)
                .sum();
    }
}

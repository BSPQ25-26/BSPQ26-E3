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

/**
 *
 * Service layer for recording and querying sales.
 *
 * Converts checked-out cart lines into Sale records and provides
 * seller-centric sales reports.
 */
@Service
public class SaleService {

    private final SaleRepository saleRepository;

    /**
     * Constructs SaleService.
     * @param saleRepository Data-access object for Sale entities.
     */
    public SaleService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    /**
     * Persists a Sale line for each item in the cart.
     *
     * Extracts the seller from every CartItem and links it to the receipt.
     *
     * @param receipt The receipt produced during checkout.
     * @param cart    The cart that was purchased.
     */
    @Transactional
    public void recordSalesFromCheckout(Receipt receipt, Cart cart) {
        for (CartItem ci : cart.getItems()) {
            Item item = ci.getItem();
            UUID sellerId = item.getSeller().getId();

            Sale sale = new Sale(sellerId, receipt, item, ci.getQuantity(), item.getAmount());
            saleRepository.save(sale);
        }
    }

    /**
     * Returns all sales made by a particular seller.
     * @param sellerId UUID of the seller.
     * @return List of SaleResponse DTOs.
     */
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

    /**
     * Calculates the total revenue for a seller.
     * @param sellerId UUID of the seller.
     * @return Sum of all sale totals for that seller.
     */
    public Double getTotalSalesBySellerId(UUID sellerId) {
        List<Sale> sales = saleRepository.findBySellerId(sellerId);
        return sales.stream()
                .mapToDouble(Sale::getTotalPrice)
                .sum();
    }
}

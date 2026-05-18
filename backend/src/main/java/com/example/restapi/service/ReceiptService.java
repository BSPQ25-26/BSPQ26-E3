package com.example.restapi.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.restapi.dto.ReceiptItemResponse;
import com.example.restapi.dto.ReceiptResponse;
import com.example.restapi.model.Cart;
import com.example.restapi.model.CartItem;
import com.example.restapi.model.Receipt;
import com.example.restapi.model.ReceiptItem;
import com.example.restapi.repository.ReceiptRepository;

import jakarta.persistence.EntityManager;

/**
 *
 * Service layer for receipt and order management.
 *
 * Creates receipts from shopping carts, retrieves buyer history and
 * delegates order-state transitions to OrderStateService.
 */
@Service
public class ReceiptService {
    
    private final EntityManager entityManager;
    private final ReceiptRepository receiptRepository;
    private final OrderStateService orderStateService;

    /**
     * Constructs ReceiptService.
     * @param entityManager       JPA EntityManager for flush/refresh.
     * @param receiptRepository   Repository for Receipt aggregates.
     * @param orderStateService   Service that advances or cancels order states.
     */
    public ReceiptService(EntityManager entityManager, ReceiptRepository receiptRepository, OrderStateService orderStateService) {
        this.entityManager = entityManager;
        this.receiptRepository = receiptRepository;
        this.orderStateService = orderStateService;
    }

    /**
     * Generates a receipt for a completed checkout.
     *
     * Calculates the total amount, builds ReceiptItem lines and persists
     * the aggregate.  The receipt number follows the pattern
     * {@code REC-<timestamp>-<randomId>}.
     *
     * @param buyerId       UUID of the purchaser.
     * @param cart          The cart that was checked out (must not be empty).
     * @param paymentStatus String describing the payment result (e.g. "COMPLETED").
     * @return The persisted Receipt entity.
     * @throws RuntimeException if the cart contains no items.
     */
    @Transactional
    public Receipt createReceipt(UUID buyerId, Cart cart, String paymentStatus) {
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot create receipt for empty cart");
        }
        String receiptNumber = "REC-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
        Double totalAmount = cart.getItems().stream()
                .mapToDouble(ci -> ci.getItem().getAmount() * ci.getQuantity())
                .sum();

        Receipt receipt = new Receipt(buyerId, receiptNumber, totalAmount, paymentStatus);

        for (CartItem ci : cart.getItems()) {
            ReceiptItem ri = new ReceiptItem(ci.getItem(), ci.getQuantity(), ci.getItem().getAmount());
            receipt.addItem(ri);
        }
        receipt = receiptRepository.save(receipt);
        entityManager.flush();
        entityManager.refresh(receipt); 

        return receipt;
    }

    /**
     * Returns every receipt belonging to a buyer, refreshing order status first.
     * @param buyerId UUID of the buyer.
     * @return List of ReceiptResponse DTOs with up-to-date order states.
     */
    @Transactional(readOnly = true)
    public List<ReceiptResponse> getReceiptsByBuyerId(UUID buyerId) {
        List<Receipt> receipts = receiptRepository.findByBuyerId(buyerId);
        return receipts.stream()
                .peek(receipt -> {
                    orderStateService.updateOrderStatus(receipt.getId());
                    entityManager.refresh(receipt);
                    receipt.getItems().size();
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single receipt by id, refreshing its order status first.
     * @param receiptId Receipt primary key.
     * @return ReceiptResponse DTO.
     * @throws RuntimeException if the receipt does not exist.
     */
    @Transactional(readOnly = true)
    public ReceiptResponse getReceiptById(Long receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));
        orderStateService.updateOrderStatus(receiptId);
        entityManager.refresh(receipt);
        receipt.getItems().size();
        return toResponse(receipt);
    }

    /**
     * Cancels an order that is still in PROCESSING state.
     * @param receiptId Receipt primary key.
     * @return ReceiptResponse DTO reflecting the cancelled state.
     * @throws RuntimeException if the order is not in PROCESSING state.
     */
    @Transactional
    public ReceiptResponse cancelOrder(Long receiptId) {
        Receipt receipt = orderStateService.cancelOrder(receiptId);
        receipt.getItems().size();
        return toResponse(receipt);
    }

    /**
     * Converts a Receipt entity into a ReceiptResponse DTO.
     * @param receipt The source entity.
     * @return A fully populated ReceiptResponse.
     */
    private ReceiptResponse toResponse(Receipt receipt) {
        List<ReceiptItemResponse> items = receipt.getItems().stream()
                .map(ri -> new ReceiptItemResponse(ri.getItem().getId(), ri.getItem().getName(), 
                        ri.getUnitPrice(), ri.getQuantity()))
                .collect(Collectors.toList());
        
        Long remainingTime = orderStateService.getRemainingTimeForCurrentState(receipt);
        
        return new ReceiptResponse(
                receipt.getId(), 
                receipt.getBuyerId(), 
                receipt.getReceiptNumber(),
                receipt.getTotalAmount(), 
                receipt.getPaymentStatus(), 
                receipt.getOrderStatus().toString(),
                receipt.getCreatedAt(),
                receipt.getProcessingStartedAt(),
                receipt.getDeliveryStartedAt(),
                receipt.getCompletedAt(),
                receipt.getCancelledAt(),
                remainingTime,
                items
        );
    }
}

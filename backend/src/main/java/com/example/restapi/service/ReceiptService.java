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

@Service
public class ReceiptService {
    
    private final EntityManager entityManager;
    private final ReceiptRepository receiptRepository;
    private final OrderStateService orderStateService;

    public ReceiptService(EntityManager entityManager, ReceiptRepository receiptRepository, OrderStateService orderStateService) {
        this.entityManager = entityManager;
        this.receiptRepository = receiptRepository;
        this.orderStateService = orderStateService;
    }

    @Transactional
    public Receipt createReceipt(UUID buyerId, Cart cart, String paymentStatus) {
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot create receipt for empty cart");
        }
        // Generate receipt number: REC-timestamp-randomId
        String receiptNumber = "REC-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
        Double totalAmount = cart.getItems().stream()
                .mapToDouble(ci -> ci.getItem().getAmount() * ci.getQuantity())
                .sum();

        Receipt receipt = new Receipt(buyerId, receiptNumber, totalAmount, paymentStatus);

        // Add items to receipt
        for (CartItem ci : cart.getItems()) {
            ReceiptItem ri = new ReceiptItem(ci.getItem(), ci.getQuantity(), ci.getItem().getAmount());
            receipt.addItem(ri);
        }
        receipt = receiptRepository.save(receipt);
        // Flush to DB and refresh from DB to get the 'created_at' refreshed timestamp
        entityManager.flush();
        entityManager.refresh(receipt); 

        return receipt;

    }

    @Transactional(readOnly = true)
    public List<ReceiptResponse> getReceiptsByBuyerId(UUID buyerId) {
        List<Receipt> receipts = receiptRepository.findByBuyerId(buyerId);
        return receipts.stream()
                .peek(receipt -> {
                    orderStateService.updateOrderStatus(receipt.getId());
                    // Force load items within transaction
                    receipt.getItems().size();
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReceiptResponse getReceiptById(Long receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));
        // Update status based on elapsed time before returning
        orderStateService.updateOrderStatus(receiptId);
        // Force load items within transaction
        receipt.getItems().size();
        return toResponse(receipt);
    }

    @Transactional
    public ReceiptResponse cancelOrder(Long receiptId) {
        Receipt receipt = orderStateService.cancelOrder(receiptId);
        // Force load items within transaction
        receipt.getItems().size();
        return toResponse(receipt);
    }

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

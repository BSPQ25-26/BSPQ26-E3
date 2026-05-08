package com.example.restapi.service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.restapi.model.OrderStatus;
import com.example.restapi.model.Receipt;
import com.example.restapi.repository.ReceiptRepository;

@Service
public class OrderStateService {

    private final ReceiptRepository receiptRepository;
    private final Random random = new Random();

    public OrderStateService(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    // Get duration for a state in seconds (simulated delivery times)
    private long getStateDurationSeconds(OrderStatus fromStatus) {
        if (fromStatus == OrderStatus.PROCESSING) {
            return 5 + random.nextLong(11); // 5-15 seconds
        } else if (fromStatus == OrderStatus.DELIVERY) {
            return 8 + random.nextLong(13); // 8-20 seconds
        }
        return 0;
    }

    // Returns true if status was changed, false otherwise
    @Transactional
    public boolean updateOrderStatus(Long receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));

        OrderStatus currentStatus = receipt.getOrderStatus();

        if (currentStatus == OrderStatus.PROCESSING) {
            if (receipt.getProcessingStartedAt() == null) {
                receipt.setProcessingStartedAt(OffsetDateTime.now());
            }
            long secondsElapsed = ChronoUnit.SECONDS.between(
                    receipt.getProcessingStartedAt(), OffsetDateTime.now());
            long requiredDuration = getStateDurationSeconds(OrderStatus.PROCESSING);

            if (secondsElapsed >= requiredDuration) {
                receipt.setOrderStatus(OrderStatus.DELIVERY);
                receipt.setDeliveryStartedAt(OffsetDateTime.now());
                receiptRepository.save(receipt);
                return true;
            }
        } else if (currentStatus == OrderStatus.DELIVERY) {
            if (receipt.getDeliveryStartedAt() == null) {
                receipt.setDeliveryStartedAt(OffsetDateTime.now());
            }
            long secondsElapsed = ChronoUnit.SECONDS.between(
                    receipt.getDeliveryStartedAt(), OffsetDateTime.now());
            long requiredDuration = getStateDurationSeconds(OrderStatus.DELIVERY);

            if (secondsElapsed >= requiredDuration) {
                receipt.setOrderStatus(OrderStatus.COMPLETED);
                receipt.setCompletedAt(OffsetDateTime.now());
                receiptRepository.save(receipt);
                return true;
            }
        }

        return false;
    }

    // Cancel an order (only if in PROCESSING state)
    @Transactional
    public Receipt cancelOrder(Long receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));

        if (receipt.getOrderStatus() != OrderStatus.PROCESSING) {
            throw new RuntimeException("Cannot cancel order in " + receipt.getOrderStatus() + " state. Only PROCESSING orders can be cancelled.");
        }

        receipt.setOrderStatus(OrderStatus.CANCELLED);
        receipt.setCancelledAt(OffsetDateTime.now());
        receiptRepository.save(receipt);
        
        return receipt;
    }

    // Get remaining time for current state in seconds
    public long getRemainingTimeForCurrentState(Receipt receipt) {
        OrderStatus status = receipt.getOrderStatus();

        if (status == OrderStatus.PROCESSING && receipt.getProcessingStartedAt() != null) {
            long elapsed = ChronoUnit.SECONDS.between(
                    receipt.getProcessingStartedAt(), OffsetDateTime.now());
            long required = getStateDurationSeconds(OrderStatus.PROCESSING);
            return Math.max(0, required - elapsed);
        } else if (status == OrderStatus.DELIVERY && receipt.getDeliveryStartedAt() != null) {
            long elapsed = ChronoUnit.SECONDS.between(
                    receipt.getDeliveryStartedAt(), OffsetDateTime.now());
            long required = getStateDurationSeconds(OrderStatus.DELIVERY);
            return Math.max(0, required - elapsed);
        }

        return 0;
    }
}

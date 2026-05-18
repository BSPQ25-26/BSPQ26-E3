package com.example.restapi.service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.restapi.model.OrderStatus;
import com.example.restapi.model.Receipt;
import com.example.restapi.repository.ReceiptRepository;

/**
 *
 * Service layer that simulates the order lifecycle.
 *
 * Manages state transitions for receipts (PROCESSING → DELIVERY → COMPLETED)
 * and supports cancellation while the order is still in PROCESSING.
 * Durations are deliberately short (seconds) so that the workflow can be
 * observed during a demo.
 */
@Service
public class OrderStateService {

    private final ReceiptRepository receiptRepository;
    private final Random random = new Random();

    /**
     * Constructs OrderStateService.
     * @param receiptRepository Repository for Receipt aggregates.
     */
    public OrderStateService(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    /**
     * Computes how many seconds a state should last.
     * @param fromStatus The current OrderStatus.
     * @return Simulated duration in seconds.
     */
    private long getStateDurationSeconds(OrderStatus fromStatus) {
        if (fromStatus == OrderStatus.PROCESSING) {
            return 5 + random.nextLong(11); // 5-15 seconds
        } else if (fromStatus == OrderStatus.DELIVERY) {
            return 8 + random.nextLong(13); // 8-20 seconds
        }
        return 0;
    }

    /**
     * Advances the order to the next state if enough time has elapsed.
     *
     * When the required duration for PROCESSING or DELIVERY is exceeded the
     * receipt is promoted to DELIVERY or COMPLETED respectively.
     *
     * @param receiptId Receipt primary key.
     * @return true if the status actually changed, false otherwise.
     * @throws RuntimeException if the receipt does not exist.
     */
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

    /**
     * Cancels an order.
     *
     * Cancellation is only permitted while the order is in PROCESSING.
     *
     * @param receiptId Receipt primary key.
     * @return The updated Receipt entity.
     * @throws RuntimeException if the receipt is not found or is not in
     *                          PROCESSING state.
     */
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

    /**
     * Calculates the remaining seconds for the current simulated state.
     * @param receipt The receipt whose remaining time is requested.
     * @return Remaining seconds (0 if no timer applies).
     */
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

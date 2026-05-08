package com.example.restapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.restapi.model.OrderStatus;
import com.example.restapi.model.Receipt;
import com.example.restapi.repository.ReceiptRepository;

@DisplayName("OrderStateService Tests")
class OrderStateServiceTest {

    @Mock
    private ReceiptRepository receiptRepository;

    @InjectMocks
    private OrderStateService orderStateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("updateOrderStatus")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("advances PROCESSING orders to DELIVERY when enough time has elapsed")
        void updateOrderStatus_advancesProcessingToDelivery() {
            Receipt receipt = new Receipt(UUID.randomUUID(), "REC-1", 50.0, "COMPLETED");
            receipt.setId(1L);
            receipt.setProcessingStartedAt(OffsetDateTime.now().minusSeconds(1000));

            when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));

            boolean progressed = orderStateService.updateOrderStatus(1L);

            assertTrue(progressed, "Order should progress from PROCESSING to DELIVERY after sufficient elapsed time");
            assertEquals(OrderStatus.DELIVERY, receipt.getOrderStatus());
            assertNotNull(receipt.getDeliveryStartedAt());
            verify(receiptRepository).save(receipt);
        }

        @Test
        @DisplayName("advances DELIVERY orders to COMPLETED when enough time has elapsed")
        void updateOrderStatus_advancesDeliveryToCompleted() {
            Receipt receipt = new Receipt(UUID.randomUUID(), "REC-2", 50.0, "COMPLETED");
            receipt.setId(2L);
            receipt.setOrderStatus(OrderStatus.DELIVERY);
            receipt.setProcessingStartedAt(OffsetDateTime.now().minusSeconds(1000));
            receipt.setDeliveryStartedAt(OffsetDateTime.now().minusSeconds(1000));

            when(receiptRepository.findById(2L)).thenReturn(Optional.of(receipt));

            boolean progressed = orderStateService.updateOrderStatus(2L);

            assertTrue(progressed, "Order should progress from DELIVERY to COMPLETED after sufficient elapsed time");
            assertEquals(OrderStatus.COMPLETED, receipt.getOrderStatus());
            assertNotNull(receipt.getCompletedAt());
            verify(receiptRepository).save(receipt);
        }

        @Test
        @DisplayName("does not advance state when not enough time has elapsed")
        void updateOrderStatus_doesNotAdvanceBeforeTimeElapsed() {
            Receipt receipt = new Receipt(UUID.randomUUID(), "REC-3", 50.0, "COMPLETED");
            receipt.setId(3L);
            receipt.setProcessingStartedAt(OffsetDateTime.now());

            when(receiptRepository.findById(3L)).thenReturn(Optional.of(receipt));

            boolean progressed = orderStateService.updateOrderStatus(3L);

            assertFalse(progressed, "Order should not advance before enough time has elapsed");
            assertEquals(OrderStatus.PROCESSING, receipt.getOrderStatus());
        }
    }

    @Nested
    @DisplayName("cancelOrder")
    class CancelOrderTests {

        @Test
        @DisplayName("cancels PROCESSING orders")
        void cancelOrder_cancelsProcessingOrder() {
            Receipt receipt = new Receipt(UUID.randomUUID(), "REC-4", 50.0, "COMPLETED");
            receipt.setId(4L);
            receipt.setProcessingStartedAt(OffsetDateTime.now().minusSeconds(10));

            when(receiptRepository.findById(4L)).thenReturn(Optional.of(receipt));

            Receipt canceled = orderStateService.cancelOrder(4L);

            assertEquals(OrderStatus.CANCELLED, canceled.getOrderStatus());
            assertNotNull(canceled.getCancelledAt());
            verify(receiptRepository).save(canceled);
        }

        @Test
        @DisplayName("throws when cancelling a non-PROCESSING order")
        void cancelOrder_throwsForNonProcessingOrder() {
            Receipt receipt = new Receipt(UUID.randomUUID(), "REC-5", 50.0, "COMPLETED");
            receipt.setId(5L);
            receipt.setOrderStatus(OrderStatus.DELIVERY);
            receipt.setDeliveryStartedAt(OffsetDateTime.now().minusSeconds(10));

            when(receiptRepository.findById(5L)).thenReturn(Optional.of(receipt));

            assertThrows(RuntimeException.class, () -> orderStateService.cancelOrder(5L));
        }
    }

    @Test
    @DisplayName("returns zero remaining seconds once the state duration has already passed")
    void getRemainingTimeForCurrentState_returnsZeroAfterDuration() {
        Receipt receipt = new Receipt(UUID.randomUUID(), "REC-6", 50.0, "COMPLETED");
        receipt.setId(6L);
        receipt.setProcessingStartedAt(OffsetDateTime.now().minusSeconds(1000));

        long remaining = orderStateService.getRemainingTimeForCurrentState(receipt);

        assertEquals(0L, remaining);
    }
}

package com.example.restapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.restapi.dto.ReceiptResponse;
import com.example.restapi.model.Cart;
import com.example.restapi.model.CartItem;
import com.example.restapi.model.Item;
import com.example.restapi.model.OrderStatus;
import com.example.restapi.model.Receipt;
import com.example.restapi.repository.ReceiptRepository;

import jakarta.persistence.EntityManager;

@DisplayName("ReceiptService Tests")
class ReceiptServiceTest {

    @Mock
    private ReceiptRepository receiptRepository;
    @Mock
    private OrderStateService orderStateService;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ReceiptService receiptService;

    private UUID buyerId;
    private Item testItem;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        doNothing().when(entityManager).refresh(any());
        doNothing().when(entityManager).flush();
        buyerId = UUID.randomUUID();

        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setAmount(25.0);
        testItem.setQuantity(100);
        testItem.setStatus(true);

        testCart = new Cart(buyerId);
        testCart.addItem(new CartItem(testItem, 2));
    }

    @Nested
    @DisplayName("createReceipt")
    class CreateReceiptTests {

        @Test
        @DisplayName("creates receipt with correct total and initial status")
        void createReceipt_createsWithCorrectData() {
            when(receiptRepository.save(any(Receipt.class))).thenAnswer(i -> {
                Receipt r = i.getArgument(0);
                r.setId(1L);
                return r;
            });

            Receipt result = receiptService.createReceipt(buyerId, testCart, "COMPLETED");

            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(buyerId, result.getBuyerId());
            assertEquals(50.0, result.getTotalAmount(), 0.001);
            assertEquals("COMPLETED", result.getPaymentStatus());
            assertEquals(OrderStatus.PROCESSING, result.getOrderStatus());
            assertNotNull(result.getProcessingStartedAt());
            assertEquals(1, result.getItems().size());

            verify(receiptRepository).save(any(Receipt.class));
            verify(entityManager).flush();
            verify(entityManager).refresh(any(Receipt.class));
        }

        @Test
        @DisplayName("throws when cart is empty")
        void createReceipt_throwsWhenCartIsEmpty() {
            Cart emptyCart = new Cart(buyerId);

            assertThrows(RuntimeException.class,
                    () -> receiptService.createReceipt(buyerId, emptyCart, "COMPLETED"));
        }

        @Test
        @DisplayName("generates unique receipt numbers")
        void createReceipt_generatesUniqueReceiptNumbers() {
            when(receiptRepository.save(any(Receipt.class))).thenAnswer(i -> {
                Receipt r = i.getArgument(0);
                r.setId(System.nanoTime());
                return r;
            });

            Receipt receipt1 = receiptService.createReceipt(buyerId, testCart, "COMPLETED");
            Receipt receipt2 = receiptService.createReceipt(buyerId, testCart, "COMPLETED");

            assertNotNull(receipt1.getReceiptNumber());
            assertNotNull(receipt2.getReceiptNumber());
            assertNotEquals(receipt1.getReceiptNumber(), receipt2.getReceiptNumber());
        }
    }

    @Nested
    @DisplayName("getReceiptsByBuyerId")
    class GetReceiptsByBuyerIdTests {

        @Test
        @DisplayName("returns list of receipts for buyer with status updates")
        void getReceiptsByBuyerId_returnsReceipts() {
            Receipt receipt1 = new Receipt(buyerId, "REC-1", 50.0, "COMPLETED");
            receipt1.setId(1L); 
            when(receiptRepository.findByBuyerId(buyerId)).thenReturn(List.of(receipt1));
            when(orderStateService.updateOrderStatus(1L)).thenReturn(true);
            when(orderStateService.getRemainingTimeForCurrentState(any())).thenReturn(10L);
            
            //Mock EntityManager refresh to do nothing since we are not testing JPA behavior here
            doNothing().when(entityManager).refresh(any(Receipt.class));
            List<ReceiptResponse> results = receiptService.getReceiptsByBuyerId(buyerId);

            assertEquals(1, results.size());
            verify(orderStateService).updateOrderStatus(1L);
            verify(entityManager).refresh(receipt1);
        }

        @Test
        @DisplayName("returns empty list when buyer has no receipts")
        void getReceiptsByBuyerId_returnsEmptyList() {
            when(receiptRepository.findByBuyerId(buyerId)).thenReturn(List.of());

            List<ReceiptResponse> results = receiptService.getReceiptsByBuyerId(buyerId);

            assertTrue(results.isEmpty());
            verify(receiptRepository).findByBuyerId(buyerId);
        }
    }

    @Nested
    @DisplayName("getReceiptById")
    class GetReceiptByIdTests {

        @Test
        @DisplayName("returns receipt response with updated order status")
        void getReceiptById_returnsReceiptWithStatusUpdate() {
            Receipt receipt = new Receipt(buyerId, "REC-100", 50.0, "COMPLETED");
            receipt.setId(100L);

            when(receiptRepository.findById(100L)).thenReturn(Optional.of(receipt));
            when(orderStateService.updateOrderStatus(100L)).thenReturn(true);
            when(orderStateService.getRemainingTimeForCurrentState(receipt)).thenReturn(10L);

            ReceiptResponse result = receiptService.getReceiptById(100L);

            assertNotNull(result);
            assertEquals(100L, result.getReceiptId());
            assertEquals(buyerId, result.getBuyerId());
            assertEquals(50.0, result.getTotalAmount(), 0.001);

            verify(receiptRepository).findById(100L);
            verify(orderStateService).updateOrderStatus(100L);
            verify(entityManager).refresh(receipt);
        }

        @Test
        @DisplayName("throws when receipt not found")
        void getReceiptById_throwsWhenNotFound() {
            when(receiptRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> receiptService.getReceiptById(999L));
        }
    }

    @Test
    @DisplayName("correctly calculates remaining time for order states")
    void receiptServiceIntegration_ordersProgressThroughStates() {
        Receipt receipt = new Receipt(buyerId, "REC-200", 100.0, "COMPLETED");
        receipt.setId(200L);
        receipt.setProcessingStartedAt(OffsetDateTime.now().minusSeconds(100));

        when(receiptRepository.findById(200L)).thenReturn(Optional.of(receipt));
        when(orderStateService.getRemainingTimeForCurrentState(receipt)).thenReturn(0L);

        ReceiptResponse response = receiptService.getReceiptById(200L);

        assertEquals("PROCESSING", response.getOrderStatus());
        verify(orderStateService).updateOrderStatus(200L);
    }
}

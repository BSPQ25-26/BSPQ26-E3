package com.example.restapi.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.restapi.dto.ReceiptResponse;
import com.example.restapi.service.OrderStateService;
import com.example.restapi.service.ReceiptService;

@WebMvcTest(PaymentController.class)
@DisplayName("PaymentController Tests")
class PaymentControllerTest {

    private static final Logger log = LoggerFactory.getLogger(PaymentControllerTest.class);

    @Autowired MockMvc mockMvc;
    @MockitoBean ReceiptService receiptService;
    @MockitoBean OrderStateService orderStateService;

    private ReceiptResponse sampleReceipt() {
        return new ReceiptResponse(1L, UUID.randomUUID(), "REC-001", 99.0,
                "PAID", "PROCESSING", null, null, null, null, null, 120L, List.of());
    }

    @Nested
    @DisplayName("GET /api/payment/order/{receiptId}")
    class GetOrderStatusTests {

        @Test
        @DisplayName("should return 200 with receipt for existing order")
        void returnsReceiptForExistingOrder() throws Exception {
            when(receiptService.getReceiptById(1L)).thenReturn(sampleReceipt());

            mockMvc.perform(get("/api/payment/order/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.receiptNumber").value("REC-001"));

            log.info("returnsReceiptForExistingOrder passed");
        }

        @Test
        @DisplayName("should return 400 with error body when order not found")
        void returns400WhenOrderNotFound() throws Exception {
            when(receiptService.getReceiptById(99L)).thenThrow(new RuntimeException("Order not found"));

            mockMvc.perform(get("/api/payment/order/99"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("ERROR"));

            log.info("returns400WhenOrderNotFound passed");
        }
    }

    @Nested
    @DisplayName("POST /api/payment/order/{receiptId}/cancel")
    class CancelOrderTests {

        @Test
        @DisplayName("should return 200 with updated receipt on successful cancel")
        void returns200OnSuccessfulCancel() throws Exception {
            when(orderStateService.cancelOrder(1L)).thenReturn(null);
            when(receiptService.getReceiptById(1L)).thenReturn(sampleReceipt());

            mockMvc.perform(post("/api/payment/order/1/cancel"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.receiptId").value(1));

            log.info("returns200OnSuccessfulCancel passed");
        }

        @Test
        @DisplayName("should return 400 with error body on RuntimeException")
        void returns400OnRuntimeException() throws Exception {
            when(orderStateService.cancelOrder(99L)).thenThrow(new RuntimeException("Cannot cancel"));

            mockMvc.perform(post("/api/payment/order/99/cancel"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("ERROR"));

            log.info("returns400OnRuntimeException passed");
        }
    }
}

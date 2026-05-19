package com.example.restapi.controller;

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
import com.example.restapi.service.ReceiptService;

@WebMvcTest(ReceiptController.class)
@DisplayName("ReceiptController Tests")
class ReceiptControllerTest {

    private static final Logger log = LoggerFactory.getLogger(ReceiptControllerTest.class);

    @Autowired MockMvc mockMvc;
    @MockitoBean ReceiptService receiptService;

    private ReceiptResponse sampleReceipt() {
        return new ReceiptResponse(1L, UUID.randomUUID(), "REC-001", 50.0,
                "PAID", "PROCESSING", null, null, null, null, null, 120L, List.of());
    }

    @Nested
    @DisplayName("GET /api/receipts/buyer/{buyerId}")
    class GetReceiptsByBuyerIdTests {

        @Test
        @DisplayName("should return 200 with receipts for valid UUID")
        void returnsReceiptsForValidBuyer() throws Exception {
            UUID buyerId = UUID.randomUUID();
            when(receiptService.getReceiptsByBuyerId(buyerId)).thenReturn(List.of(sampleReceipt()));

            mockMvc.perform(get("/api/receipts/buyer/" + buyerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].receiptId").value(1));

            log.info("returnsReceiptsForValidBuyer passed");
        }

        @Test
        @DisplayName("should return 400 for invalid UUID")
        void returns400ForInvalidUUID() throws Exception {
            mockMvc.perform(get("/api/receipts/buyer/not-a-uuid"))
                    .andExpect(status().isBadRequest());

            log.info("returns400ForInvalidUUID passed");
        }
    }

    @Nested
    @DisplayName("GET /api/receipts/{receiptId}")
    class GetReceiptByIdTests {

        @Test
        @DisplayName("should return 200 with receipt for existing id")
        void returnsReceiptForExistingId() throws Exception {
            when(receiptService.getReceiptById(1L)).thenReturn(sampleReceipt());

            mockMvc.perform(get("/api/receipts/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.receiptNumber").value("REC-001"));

            log.info("returnsReceiptForExistingId passed");
        }

        @Test
        @DisplayName("should return 404 when receipt not found")
        void returns404ForMissingReceipt() throws Exception {
            when(receiptService.getReceiptById(99L)).thenThrow(new RuntimeException("Not found"));

            mockMvc.perform(get("/api/receipts/99"))
                    .andExpect(status().isNotFound());

            log.info("returns404ForMissingReceipt passed");
        }
    }

    @Nested
    @DisplayName("POST /api/receipts/{receiptId}/cancel")
    class CancelOrderTests {

        @Test
        @DisplayName("should return 200 when order cancelled successfully")
        void returns200OnSuccessfulCancel() throws Exception {
            when(receiptService.cancelOrder(1L)).thenReturn(sampleReceipt());

            mockMvc.perform(post("/api/receipts/1/cancel"))
                    .andExpect(status().isOk());

            log.info("returns200OnSuccessfulCancel passed");
        }

        @Test
        @DisplayName("should return 400 when cancel fails")
        void returns400OnCancelFailure() throws Exception {
            when(receiptService.cancelOrder(99L)).thenThrow(new RuntimeException("Cannot cancel"));

            mockMvc.perform(post("/api/receipts/99/cancel"))
                    .andExpect(status().isBadRequest());

            log.info("returns400OnCancelFailure passed");
        }
    }
}

package com.example.restapi.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.example.restapi.dto.SaleResponse;
import com.example.restapi.service.SaleService;

@WebMvcTest(SalesController.class)
@DisplayName("SalesController Tests")
class SalesControllerTest {

    private static final Logger log = LoggerFactory.getLogger(SalesControllerTest.class);

    @Autowired MockMvc mockMvc;
    @MockitoBean SaleService saleService;

    @Nested
    @DisplayName("GET /api/sales/seller/{sellerId}")
    class GetSalesBySellerIdTests {

        @Test
        @DisplayName("should return 200 with sales list for valid seller UUID")
        void returnsSalesForValidSeller() throws Exception {
            UUID sellerId = UUID.randomUUID();
            SaleResponse sale = new SaleResponse(1L, "Orchid", 2, 15.0, 30.0, null);
            when(saleService.getSalesBySellerId(sellerId)).thenReturn(List.of(sale));

            mockMvc.perform(get("/api/sales/seller/" + sellerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].itemName").value("Orchid"))
                    .andExpect(jsonPath("$[0].quantity").value(2));

            log.info("returnsSalesForValidSeller passed");
        }

        @Test
        @DisplayName("should return 200 with empty list when seller has no sales")
        void returnsEmptyListForSellerWithNoSales() throws Exception {
            UUID sellerId = UUID.randomUUID();
            when(saleService.getSalesBySellerId(sellerId)).thenReturn(List.of());

            mockMvc.perform(get("/api/sales/seller/" + sellerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());

            log.info("returnsEmptyListForSellerWithNoSales passed");
        }

        @Test
        @DisplayName("should return 400 for invalid UUID")
        void returns400ForInvalidUUID() throws Exception {
            mockMvc.perform(get("/api/sales/seller/not-a-uuid"))
                    .andExpect(status().isBadRequest());

            log.info("returns400ForInvalidUUID passed");
        }
    }

    @Nested
    @DisplayName("GET /api/sales/seller/{sellerId}/total")
    class GetTotalSalesBySellerIdTests {

        @Test
        @DisplayName("should return 200 with total, count and sales list")
        void returnsTotalForValidSeller() throws Exception {
            UUID sellerId = UUID.randomUUID();
            SaleResponse sale = new SaleResponse(1L, "Fern", 3, 10.0, 30.0, null);
            when(saleService.getTotalSalesBySellerId(sellerId)).thenReturn(30.0);
            when(saleService.getSalesBySellerId(sellerId)).thenReturn(List.of(sale));

            mockMvc.perform(get("/api/sales/seller/" + sellerId + "/total"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalSales").value(30.0))
                    .andExpect(jsonPath("$.numberOfSales").value(1));

            log.info("returnsTotalForValidSeller passed");
        }

        @Test
        @DisplayName("should return 400 for invalid UUID")
        void returns400ForInvalidUUID() throws Exception {
            mockMvc.perform(get("/api/sales/seller/bad-uuid/total"))
                    .andExpect(status().isBadRequest());

            log.info("returns400ForInvalidUUID passed");
        }
    }
}

package com.example.restapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.restapi.dto.SaleResponse;
import com.example.restapi.model.Cart;
import com.example.restapi.model.CartItem;
import com.example.restapi.model.Item;
import com.example.restapi.model.Profile;
import com.example.restapi.model.Receipt;
import com.example.restapi.model.Sale;
import com.example.restapi.repository.SaleRepository;

@DisplayName("SaleService Tests")
class SaleServiceTest {

    private static final Logger log = LoggerFactory.getLogger(SaleServiceTest.class);

    @Mock private SaleRepository saleRepository;

    private SaleService saleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        saleService = new SaleService(saleRepository);
    }

    @Nested
    @DisplayName("recordSalesFromCheckout")
    class RecordSalesFromCheckoutTests {

        @Test
        @DisplayName("should create one Sale per cart item")
        void createsOneSalePerCartItem() {
            UUID sellerId = UUID.randomUUID();
            Profile seller = new Profile(sellerId, "seller", "000");

            Item item1 = new Item("Rose", "Red rose", 5.0, null, 10, null, true);
            item1.setSeller(seller);
            Item item2 = new Item("Cactus", "Small cactus", 8.0, null, 5, null, true);
            item2.setSeller(seller);

            CartItem ci1 = new CartItem(item1, 2);
            CartItem ci2 = new CartItem(item2, 1);

            Cart cart = new Cart(UUID.randomUUID());
            cart.addItem(ci1);
            cart.addItem(ci2);

            Receipt receipt = new Receipt();

            saleService.recordSalesFromCheckout(receipt, cart);

            verify(saleRepository, times(2)).save(any(Sale.class));
            log.info("createsOneSalePerCartItem passed: 2 sales saved");
        }

        @Test
        @DisplayName("should do nothing when cart is empty")
        void doesNothingForEmptyCart() {
            Cart cart = new Cart(UUID.randomUUID());
            Receipt receipt = new Receipt();

            saleService.recordSalesFromCheckout(receipt, cart);

            verify(saleRepository, never()).save(any());
            log.info("doesNothingForEmptyCart passed");
        }
    }

    @Nested
    @DisplayName("getSalesBySellerId")
    class GetSalesBySellerIdTests {

        @Test
        @DisplayName("should return mapped SaleResponse list for seller")
        void returnsMappedSaleResponses() {
            UUID sellerId = UUID.randomUUID();

            Profile seller = new Profile(sellerId, "seller", "000");
            Item item = new Item("Orchid", "Pretty flower", 15.0, null, 3, null, true);
            item.setSeller(seller);

            Sale sale = new Sale(sellerId, new Receipt(), item, 2, 15.0);
            sale.setId(1L);

            when(saleRepository.findBySellerId(sellerId)).thenReturn(List.of(sale));

            List<SaleResponse> result = saleService.getSalesBySellerId(sellerId);

            assertEquals(1, result.size());
            assertEquals("Orchid", result.get(0).getItemName());
            assertEquals(2, result.get(0).getQuantity());
            assertEquals(15.0, result.get(0).getUnitPrice());
            assertEquals(30.0, result.get(0).getTotalPrice());
            log.info("returnsMappedSaleResponses passed: sellerId={}", sellerId);
        }

        @Test
        @DisplayName("should return empty list when seller has no sales")
        void returnsEmptyListForNoSales() {
            UUID sellerId = UUID.randomUUID();
            when(saleRepository.findBySellerId(sellerId)).thenReturn(List.of());

            List<SaleResponse> result = saleService.getSalesBySellerId(sellerId);

            assertTrue(result.isEmpty());
            log.info("returnsEmptyListForNoSales passed");
        }
    }

    @Nested
    @DisplayName("getTotalSalesBySellerId")
    class GetTotalSalesBySellerIdTests {

        @Test
        @DisplayName("should sum total prices for all seller sales")
        void sumsTotalPricesCorrectly() {
            UUID sellerId = UUID.randomUUID();

            Profile seller = new Profile(sellerId, "seller", "000");
            Item item = new Item("Fern", "Green fern", 10.0, null, 5, null, true);
            item.setSeller(seller);

            Sale sale1 = new Sale(sellerId, new Receipt(), item, 2, 10.0); // total 20.0
            Sale sale2 = new Sale(sellerId, new Receipt(), item, 3, 10.0); // total 30.0

            when(saleRepository.findBySellerId(sellerId)).thenReturn(List.of(sale1, sale2));

            Double total = saleService.getTotalSalesBySellerId(sellerId);

            assertEquals(50.0, total, 0.001);
            log.info("sumsTotalPricesCorrectly passed: total={}", total);
        }

        @Test
        @DisplayName("should return zero when seller has no sales")
        void returnsZeroForNoSales() {
            UUID sellerId = UUID.randomUUID();
            when(saleRepository.findBySellerId(sellerId)).thenReturn(List.of());

            Double total = saleService.getTotalSalesBySellerId(sellerId);

            assertEquals(0.0, total, 0.001);
            log.info("returnsZeroForNoSales passed");
        }
    }
}

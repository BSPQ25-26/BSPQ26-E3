package com.example.restapi.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.restapi.dto.CartResponse;
import com.example.restapi.dto.PaymentRequest;
import com.example.restapi.dto.ReceiptItemResponse;
import com.example.restapi.dto.ReceiptResponse;
import com.example.restapi.model.Cart;
import com.example.restapi.model.CartItem;
import com.example.restapi.model.Item;
import com.example.restapi.model.Profile;
import com.example.restapi.model.Receipt;
import com.example.restapi.repository.CartItemRepository;
import com.example.restapi.repository.CartRepository;
import com.example.restapi.repository.ItemRepository;
import com.example.restapi.repository.ProfileRepository;


@DisplayName("CartService Tests")
class CartServiceTest {

    private static final Logger log = LoggerFactory.getLogger(CartServiceTest.class);

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ReceiptService receiptService;
    @Mock
    private SaleService saleService;
    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private CartService cartService;

    private UUID ownerId;
    private Item testItem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ownerId = UUID.randomUUID();

        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setAmount(5.0);
        testItem.setQuantity(10);
        testItem.setStatus(true);
    }

    // ── addItemToCart ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("addItemToCart")
    class AddItemTests {

        @Test
        @DisplayName("creates new cart and adds item when no cart exists")
        void addItem_createsCartAndAddsItem() {
            when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
            when(cartRepository.findByOwnerId(ownerId)).thenReturn(Optional.empty());
            when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(cartItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            CartResponse resp = cartService.addItemToCart(ownerId, 1L, 2);

            List<?> items = resp.getItems();
            assertEquals(1, items.size());
            assertEquals(10.0, resp.getTotal(), 0.001);
            verify(cartItemRepository).save(any(CartItem.class));
            verify(cartRepository, atLeastOnce()).save(any(Cart.class));
            log.info("addItem_createsCartAndAddsItem passed, total={}", resp.getTotal());
        }

        @Test
        @DisplayName("increments quantity when item already in cart")
        void addItem_incrementsQuantity_whenItemAlreadyInCart() {
            CartItem existing = new CartItem(testItem, 2);
            Cart cart = new Cart(ownerId);
            cart.addItem(existing);

            when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
            when(cartRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(cart));
            when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(cartItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            CartResponse resp = cartService.addItemToCart(ownerId, 1L, 3);

            assertEquals(1, resp.getItems().size());
            assertEquals(5, resp.getItems().get(0).getQuantity()); // 2 + 3
            verify(cartItemRepository).save(existing);
            log.info("addItem_incrementsQuantity passed, quantity={}", resp.getItems().get(0).getQuantity());
        }

        @Test
        @DisplayName("throws IllegalArgumentException when quantity is null")
        void addItem_throwsException_whenQuantityIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> cartService.addItemToCart(ownerId, 1L, null));
        }

        @Test
        @DisplayName("throws IllegalArgumentException when quantity is zero")
        void addItem_throwsException_whenQuantityIsZero() {
            assertThrows(IllegalArgumentException.class,
                    () -> cartService.addItemToCart(ownerId, 1L, 0));
        }

        @Test
        @DisplayName("throws RuntimeException when item not found")
        void addItem_throwsException_whenItemNotFound() {
            when(itemRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                    () -> cartService.addItemToCart(ownerId, 99L, 1));
            verify(itemRepository).findById(99L);
        }

        @Test
        @DisplayName("throws RuntimeException when item is unavailable")
        void addItem_throwsException_whenItemUnavailable() {
            Item unavailable = new Item();
            unavailable.setId(2L);
            unavailable.setStatus(false);
            when(itemRepository.findById(2L)).thenReturn(Optional.of(unavailable));

            assertThrows(RuntimeException.class,
                    () -> cartService.addItemToCart(ownerId, 2L, 1));
        }

        @Test
        @DisplayName("throws RuntimeException when stock is insufficient")
        void addItem_throwsException_whenNotEnoughStock() {
            Item lowStock = new Item();
            lowStock.setId(3L);
            lowStock.setName("Low Stock");
            lowStock.setAmount(10.0);
            lowStock.setQuantity(1);
            lowStock.setStatus(true);

            when(itemRepository.findById(3L)).thenReturn(Optional.of(lowStock));

            assertThrows(RuntimeException.class,
                    () -> cartService.addItemToCart(ownerId, 3L, 5));
        }
    }

    // ── removeItemFromCart ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("removeItemFromCart")
    class RemoveItemTests {

        @Test
        @DisplayName("removes existing item from cart")
        void removeItem_removesExistingItem() {
            Cart cart = new Cart(ownerId);
            CartItem ci = new CartItem(testItem, 3);
            cart.addItem(ci);

            when(cartRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(cart));
            when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            CartResponse resp = cartService.removeItemFromCart(ownerId, 1L);

            assertTrue(resp.getItems().isEmpty(), "Cart should be empty after removal");
            verify(cartItemRepository).delete(ci);
            verify(cartRepository, atLeastOnce()).save(any(Cart.class));
        }

        @Test
        @DisplayName("returns empty cart when item not present")
        void removeItem_noOp_whenItemNotInCart() {
            Cart cart = new Cart(ownerId);
            when(cartRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(cart));
            when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            CartResponse resp = cartService.removeItemFromCart(ownerId, 999L);

            assertTrue(resp.getItems().isEmpty());
            verify(cartItemRepository, never()).delete(any());
        }
    }

    // ── getCart ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getCart")
    class GetCartTests {

        @Test
        @DisplayName("returns existing cart with correct total")
        void getCart_returnsExistingCart() {
            Cart cart = new Cart(ownerId);
            cart.addItem(new CartItem(testItem, 2)); 

            when(cartRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(cart));

            CartResponse resp = cartService.getCart(ownerId);

            assertEquals(1, resp.getItems().size());
            assertEquals(10.0, resp.getTotal(), 0.001);
            log.info("getCart returned total={}", resp.getTotal());
        }

        @Test
        @DisplayName("creates and returns empty cart when none exists")
        void getCart_createsEmptyCart_whenNoneExists() {
            when(cartRepository.findByOwnerId(ownerId)).thenReturn(Optional.empty());
            when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            CartResponse resp = cartService.getCart(ownerId);

            assertTrue(resp.getItems().isEmpty());
            assertEquals(0.0, resp.getTotal(), 0.001);
        }
    }

    // ── clearCart ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("clearCart")
    class ClearCartTests {

        @Test
        @DisplayName("clears all items and resets total to zero")
        void clearCart_removesAllItems() {
            Cart cart = new Cart(ownerId);
            cart.addItem(new CartItem(testItem, 3));

            when(cartRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(cart));
            when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            CartResponse resp = cartService.clearCart(ownerId);

            assertTrue(resp.getItems().isEmpty());
            assertEquals(0.0, resp.getTotal(), 0.001);
            verify(cartRepository).save(cart);
        }
    }

    // ── checkout ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("checkout")
    class CheckoutTests {

        @Test
        @DisplayName("completes checkout and empties the cart when payment succeeds")
        void checkout_completesAndClearsCart_whenPaymentSucceeds() {
            Cart cart = new Cart(ownerId);
            CartItem cartItem = new CartItem(testItem, 2);
            cart.addItem(cartItem);

            Profile buyer = new Profile();
            buyer.setId(ownerId);

            Receipt receipt = new Receipt();
            receipt.setId(100L);

            ReceiptResponse expectedResponse = new ReceiptResponse(
                    100L,
                    ownerId,
                    "REC-1234",
                    10.0,
                    "COMPLETED",
                    "PROCESSING",
                    null,
                    null,
                    null,
                    null,
                    null,
                    0L,
                    List.<ReceiptItemResponse>of()
            );

            when(cartRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(cart));
            when(profileRepository.findById(ownerId)).thenReturn(Optional.of(buyer));
            when(paymentService.processPayment(any(PaymentRequest.class), eq(10.0))).thenReturn(true);
            when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
            when(receiptService.createReceipt(ownerId, cart, "COMPLETED")).thenReturn(receipt);
            when(receiptService.getReceiptById(100L)).thenReturn(expectedResponse);
            when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            ReceiptResponse response = cartService.checkout(ownerId, new PaymentRequest());

            assertEquals(100L, response.getReceiptId());
            assertEquals(10.0, response.getTotalAmount(), 0.001);
            assertEquals("COMPLETED", response.getPaymentStatus());
            assertTrue(response.getItems().isEmpty(), "Receipt should contain no items in mocked response");
            assertTrue(cart.getItems().isEmpty(), "Cart should be cleared after successful checkout");
            verify(receiptService).createReceipt(ownerId, cart, "COMPLETED");
            verify(saleService).recordSalesFromCheckout(receipt, cart);
            verify(cartRepository).save(cart);
        }

        @Test
        @DisplayName("throws when checkout is attempted on an empty cart")
        void checkout_throwsWhenCartIsEmpty() {
            Cart cart = new Cart(ownerId);

            Profile buyer = new Profile();
            buyer.setId(ownerId);

            when(cartRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(cart));
            when(profileRepository.findById(ownerId)).thenReturn(Optional.of(buyer));

            assertThrows(RuntimeException.class, () -> cartService.checkout(ownerId, new PaymentRequest()));
            verify(paymentService, never()).processPayment(any(), any());
            verify(receiptService, never()).createReceipt(any(), any(), any());
        }

        @Test
        @DisplayName("throws when payment is invalid")
        void checkout_throwsWhenPaymentFails() {
            Cart cart = new Cart(ownerId);
            cart.addItem(new CartItem(testItem, 1));

            Profile buyer = new Profile();
            buyer.setId(ownerId);

            when(cartRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(cart));
            when(profileRepository.findById(ownerId)).thenReturn(Optional.of(buyer));
            when(paymentService.processPayment(any(PaymentRequest.class), eq(5.0))).thenReturn(false);

            assertThrows(RuntimeException.class, () -> cartService.checkout(ownerId, new PaymentRequest()));
            verify(receiptService, never()).createReceipt(any(), any(), any());
        }
    }
}

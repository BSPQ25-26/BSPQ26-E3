package com.example.restapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.restapi.dto.CartResponse;
import com.example.restapi.dto.CartItemResponse;
import com.example.restapi.model.Cart;
import com.example.restapi.model.CartItem;
import com.example.restapi.model.Item;
import com.example.restapi.repository.CartItemRepository;
import com.example.restapi.repository.CartRepository;
import com.example.restapi.repository.ItemRepository;
import com.example.restapi.repository.ProfileRepository;

import java.util.List;

class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    private CartItemRepository cartItemRepository = mock(CartItemRepository.class);
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private CartService cartService;

    private UUID ownerId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ownerId = UUID.randomUUID();
    }

    @Test
    void addItem_createsCartAndAddsItem() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setAmount(5.0);
        item.setQuantity(10);
        item.setStatus(true);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(cartRepository.findByOwnerId(ownerId)).thenReturn(Optional.empty());
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(cartItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CartResponse resp = cartService.addItemToCart(ownerId, 1L, 2);

        List<CartItemResponse> items = resp.getItems();
        assertEquals(1, items.size(), "Should have one cart item");
        CartItemResponse cir = items.get(0);
        assertEquals(1L, cir.getItemId());
        assertEquals(2, cir.getQuantity());
        assertEquals(10.0, resp.getTotal(), 0.001);

        verify(cartItemRepository).save(any(CartItem.class));
        verify(cartRepository, atLeastOnce()).save(any(Cart.class));
    }

    @Test
    void removeItem_removesExistingItem() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setAmount(5.0);
        item.setQuantity(10);
        item.setStatus(true);

        Cart cart = new Cart(ownerId);
        CartItem ci = new CartItem(item, 3);
        cart.addItem(ci);

        when(cartRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CartResponse resp = cartService.removeItemFromCart(ownerId, 1L);

        assertTrue(resp.getItems().isEmpty(), "Cart should be empty after removal");
        verify(cartItemRepository).delete(ci);
        verify(cartRepository, atLeastOnce()).save(any(Cart.class));
    }
}
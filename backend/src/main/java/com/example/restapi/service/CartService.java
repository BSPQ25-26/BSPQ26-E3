package com.example.restapi.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.restapi.dto.CartItemResponse;
import com.example.restapi.dto.CartResponse;
import com.example.restapi.dto.PaymentRequest;
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

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;
    private final ProfileRepository profileRepository;
    private final ReceiptService receiptService;
    private final SaleService saleService;
    private final PaymentService paymentService;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ItemRepository itemRepository,
                       ProfileRepository profileRepository,
                       ReceiptService receiptService,
                       SaleService saleService,
                       PaymentService paymentService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.itemRepository = itemRepository;
        this.profileRepository = profileRepository;
        this.receiptService = receiptService;
        this.saleService = saleService;
        this.paymentService = paymentService;
    }

    public Cart getOrCreateCart(UUID ownerId) {
        return cartRepository.findByOwnerId(ownerId).orElseGet(() -> {
            Cart c = new Cart(ownerId);
            return cartRepository.save(c);
        });
    }

    //Use of @Transactional so the operations don't interrupt midway and leave the cart in an inconsistent state
    @Transactional
    public CartResponse addItemToCart(UUID ownerId, Long itemId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found"));
        if (!item.getStatus()) {
            throw new RuntimeException("Item is not available");
        }
        if (item.getQuantity() < quantity) {
            throw new RuntimeException("Not enough stock");
        }
        Cart cart = getOrCreateCart(ownerId);
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(ci -> ci.getItem().getId().equals(itemId))
                .findFirst();
        if (existing.isPresent()) {
            CartItem ci = existing.get();
            ci.setQuantity(ci.getQuantity() + quantity);
            cartItemRepository.save(ci);
        } else {
            CartItem ci = new CartItem(item, quantity);
            cart.addItem(ci);
            cartItemRepository.save(ci);
        }
        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeItemFromCart(UUID ownerId, Long itemId) {
        Cart cart = getOrCreateCart(ownerId);
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(ci -> ci.getItem().getId().equals(itemId))
                .findFirst();
        if (existing.isPresent()) {
            CartItem ci = existing.get();
            cart.removeItem(ci);
            cartItemRepository.delete(ci);
            cartRepository.save(cart);
        }
        return toResponse(cart);
    }

    @Transactional
    public CartResponse clearCart(UUID ownerId) {
        Cart cart = getOrCreateCart(ownerId);
        cart.getItems().clear();
        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional
    public ReceiptResponse checkout(UUID ownerId, PaymentRequest paymentRequest) {
        Cart cart = getOrCreateCart(ownerId);
        Profile buyer = profileRepository.findById(ownerId).orElseThrow(() -> new RuntimeException("Buyer not found"));
        
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot checkout with empty cart");
        }

        double totalAmount = cart.getItems().stream()
                .mapToDouble(ci -> ci.getItem().getAmount() * ci.getQuantity())
                .sum();

        if (!paymentService.processPayment(paymentRequest, totalAmount)) {
            throw new RuntimeException("Payment failed or invalid payment details");
        }

        // Check stock and update
        for (CartItem ci : cart.getItems()) {
            Item item = itemRepository.findById(ci.getItem().getId()).orElseThrow(() -> new RuntimeException("Item not found during checkout"));
            if (!item.getStatus() || item.getQuantity() < ci.getQuantity()) {
                throw new RuntimeException("Insufficient stock for item: " + item.getId());
            }
            item.setQuantity(item.getQuantity() - ci.getQuantity());
            itemRepository.save(item);
        }

        Receipt receipt = receiptService.createReceipt(ownerId, cart, "COMPLETED");
        saleService.recordSalesFromCheckout(receipt, cart);
        
        cart.getItems().clear();
        cartRepository.save(cart);
        
        return receiptService.getReceiptById(receipt.getId());
    }

    public CartResponse getCart(UUID ownerId) {
        Cart cart = getOrCreateCart(ownerId);
        return toResponse(cart);
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(ci -> new CartItemResponse(ci.getItem().getId(), ci.getItem().getName(), ci.getItem().getAmount(), ci.getQuantity()))
                .collect(Collectors.toList());
        double total = items.stream().mapToDouble(i -> i.getAmount() * i.getQuantity()).sum();
        return new CartResponse(cart.getId(), items, total);
    }
}

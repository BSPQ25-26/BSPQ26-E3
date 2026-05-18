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

/**
 *
 * Service layer for shopping-cart operations.
 *
 * This class exposes the remote business interface for managing a user's
 * shopping cart.  It allows clients to add/remove items, clear the cart
 * and perform a full checkout (payment + stock update + receipt generation).
 *
 * All write operations are executed inside Spring transactions so the cart
 * never ends up in an inconsistent state.
 */
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;
    private final ProfileRepository profileRepository;
    private final ReceiptService receiptService;
    private final SaleService saleService;
    private final PaymentService paymentService;

    /**
     * Constructs CartService with all required collaborators.
     * @param cartRepository          Repository for Cart aggregates.
     * @param cartItemRepository      Repository for individual cart lines.
     * @param itemRepository          Repository for catalogue items.
     * @param profileRepository       Repository for user profiles.
     * @param receiptService          Service that creates receipts.
     * @param saleService             Service that records sales.
     * @param paymentService          Service that processes payments.
     */
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

    /**
     * Retrieves the active cart for a user, creating one if necessary.
     * @param ownerId UUID of the cart owner.
     * @return The existing or newly created Cart entity.
     */
    public Cart getOrCreateCart(UUID ownerId) {
        return cartRepository.findByOwnerId(ownerId).orElseGet(() -> {
            Cart c = new Cart(ownerId);
            return cartRepository.save(c);
        });
    }

    /**
     * Adds an item to the user's cart.
     *
     * Validates stock availability and item status before updating the cart.
     * If the item is already present its quantity is incremented.
     *
     * @param ownerId  UUID of the cart owner.
     * @param itemId   Identifier of the catalogue item.
     * @param quantity Number of units to add (must be > 0).
     * @return CartResponse reflecting the updated cart state.
     * @throws IllegalArgumentException if quantity is null or &lt;= 0.
     * @throws RuntimeException         if the item does not exist, is unavailable
     *                                  or there is insufficient stock.
     */
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

    /**
     * Removes an item line from the user's cart.
     * @param ownerId UUID of the cart owner.
     * @param itemId  Identifier of the catalogue item to remove.
     * @return CartResponse reflecting the updated cart state.
     */
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

    /**
     * Empties the user's cart.
     * @param ownerId UUID of the cart owner.
     * @return CartResponse with an empty item list.
     */
    @Transactional
    public CartResponse clearCart(UUID ownerId) {
        Cart cart = getOrCreateCart(ownerId);
        cart.getItems().clear();
        cartRepository.save(cart);
        return toResponse(cart);
    }

    /**
     * Finalises the purchase (checkout).
     *
     * Validates that the cart is not empty, processes payment, deducts stock,
     * creates a receipt, records sales and clears the cart.
     *
     * @param ownerId        UUID of the buyer.
     * @param paymentRequest Payment details (card holder, number, expiry, CVV).
     * @return ReceiptResponse with the generated receipt.
     * @throws RuntimeException if the cart is empty, payment fails or stock is
     *                          insufficient during checkout.
     */
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

    /**
     * Returns the current state of a user's cart.
     * @param ownerId UUID of the cart owner.
     * @return CartResponse with the item list and total amount.
     */
    public CartResponse getCart(UUID ownerId) {
        Cart cart = getOrCreateCart(ownerId);
        return toResponse(cart);
    }

    /**
     * Converts a Cart entity into its DTO representation.
     * @param cart The cart to convert.
     * @return A populated CartResponse.
     */
    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(ci -> new CartItemResponse(ci.getItem().getId(), ci.getItem().getName(), ci.getItem().getAmount(), ci.getQuantity()))
                .collect(Collectors.toList());
        double total = items.stream().mapToDouble(i -> i.getAmount() * i.getQuantity()).sum();
        return new CartResponse(cart.getId(), items, total);
    }
}

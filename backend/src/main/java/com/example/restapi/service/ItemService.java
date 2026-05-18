package com.example.restapi.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.restapi.dto.ItemResponse;
import com.example.restapi.model.Item;
import com.example.restapi.model.Category;
import com.example.restapi.model.Profile;
import com.example.restapi.repository.ItemRepository;
import com.example.restapi.repository.CategoryRepository;
import com.example.restapi.repository.ProfileRepository;

/**
 *
 * Service layer for catalogue-item management.
 *
 * Provides CRUD operations for items, stock handling and category
 * resolution.  This class acts as the remote business facade exposed
 * to the REST controllers.
 */
@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final ProfileRepository profileRepository;

    /**
     * Constructs ItemService with its data-access collaborators.
     * @param itemRepository      Repository for Item entities.
     * @param categoryRepository  Repository for Category entities.
     * @param profileRepository   Repository for user profiles (sellers).
     */
    public ItemService(ItemRepository itemRepository, CategoryRepository categoryRepository, ProfileRepository profileRepository) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.profileRepository = profileRepository;
    }

    /**
     * Returns every item in the catalogue.
     * @return List of ItemResponse DTOs.
     */
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems() {
        return itemRepository.findAll().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Returns only items that are currently available for sale.
     * @return List of active ItemResponse DTOs.
     */
    @Transactional(readOnly = true)
    public List<ItemResponse> getActiveItems() {
        return itemRepository.findByStatusTrue().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves a single item by its unique identifier.
     * @param id The item primary key.
     * @return ItemResponse DTO.
     * @throws RuntimeException if the item does not exist.
     */
    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long id) {
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Item not found"));
        return convertToResponse(item);
    }

    /**
     * Converts an Item entity into an ItemResponse DTO.
     * @param item The source entity.
     * @return A fully populated ItemResponse.
     */
    private ItemResponse convertToResponse(Item item) {
        return new ItemResponse(
            item.getId(),
            item.getName(),
            item.getDescription(),
            item.getAmount(),
            item.getImage_URL(),
            item.getQuantity(),
            item.getStatus(),
            item.getCategory() != null ? item.getCategory().getName() : "Uncategorized",
            item.getSeller() != null ? item.getSeller().getId().toString() : null
        );
    }

    /**
     * Creates a new item and associates it with a seller.
     *
     * If the supplied category does not yet exist in the database it is
     * created automatically.
     *
     * @param item     The item to persist (may contain a transient Category).
     * @param sellerId UUID of the seller that owns the item.
     * @return The persisted Item entity with its generated id.
     * @throws RuntimeException if the seller profile is not found.
     */
    public Item createItem(Item item, UUID sellerId) {
        Profile seller = profileRepository.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("Seller not found"));
        item.setSeller(seller);

        if (item.getCategory() != null && item.getCategory().getId() == null) {
            String categoryName = item.getCategory().getName();
            Category category = categoryRepository.findByName(categoryName)
                .orElseGet(() -> {
                    Category newCategory = new Category(categoryName, "");
                    return categoryRepository.save(newCategory);
                });
            item.setCategory(category);
        }

        return itemRepository.save(item);
    }

    /**
     * Applies a partial update to an existing item.
     * @param id          Identifier of the item to update.
     * @param itemDetails Object containing the fields to overwrite.
     * @return The updated Item entity.
     * @throws RuntimeException if the item does not exist.
     */
    public Item updateItem(Long id, Item itemDetails) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        if (itemDetails.getName() != null) {
            item.setName(itemDetails.getName());
        }
        if (itemDetails.getDescription() != null) {
            item.setDescription(itemDetails.getDescription());
        }
        if (itemDetails.getAmount() != null) {
            item.setAmount(itemDetails.getAmount());
        }
        if (itemDetails.getImage_URL() != null) {
            item.setImagen(itemDetails.getImage_URL());
        }
        if (itemDetails.getQuantity() != null) {
            item.setQuantity(itemDetails.getQuantity());
        }
        if (itemDetails.getStatus() != null) {
            item.setStatus(itemDetails.getStatus());
        }
        if (itemDetails.getCategory() != null) {
            item.setCategory(itemDetails.getCategory());
        }
        return itemRepository.save(item);
    }

    /**
     * Deletes an item from the catalogue.
     * @param id Identifier of the item to remove.
     */
    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }
}

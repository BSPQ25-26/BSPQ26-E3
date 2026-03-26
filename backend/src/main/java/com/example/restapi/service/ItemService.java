package com.example.restapi.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.example.restapi.dto.ItemResponse;
import com.example.restapi.model.Item;
import com.example.restapi.repository.ItemRepository;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<ItemResponse> getAllItems() {
        return itemRepository.findAll().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    public List<ItemResponse> getActiveItems() {
        return itemRepository.findByStatusTrue().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

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

    public Item createItem(Item item) {
        return itemRepository.save(item);
    }

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

    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }
}

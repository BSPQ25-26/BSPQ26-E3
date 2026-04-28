package com.example.restapi.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.restapi.dto.ItemResponse;
import com.example.restapi.model.Category;
import com.example.restapi.model.Item;
import com.example.restapi.model.Profile;
import com.example.restapi.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@DisplayName("ItemService Tests")
public class ItemServiceTest {

    private static final Logger log = LoggerFactory.getLogger(ItemServiceTest.class);

    @Mock
    private ItemRepository itemRepository;

    private ItemService itemService;
    private Item testItem;
    private Category testCategory;
    private Profile testSeller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        itemService = new ItemService(itemRepository);

        testCategory = new Category();
        testCategory.setName("Electronics");

        testSeller = new Profile(UUID.randomUUID(), "seller", "1234567890");

        testItem = new Item("Test Item", "Test Description", 99.99, "http://image.url", 5, testCategory, true);
        testItem.setId(1L);
        testItem.setSeller(testSeller);
    }

    @Nested
    @DisplayName("getAllItems")
    class GetAllItemsTests {

        @Test
        @DisplayName("should return all items as responses")
        void testGetAllItems() {
            List<Item> items = List.of(testItem);
            when(itemRepository.findAll()).thenReturn(items);

            List<ItemResponse> result = itemService.getAllItems();

            assertEquals(1, result.size());
            assertEquals("Test Item", result.get(0).getTitle());
            verify(itemRepository, times(1)).findAll();
            log.info("testGetAllItems passed: returned {} item(s)", result.size());
        }

        @Test
        @DisplayName("should return empty list when no items exist")
        void testGetAllItemsEmpty() {
            when(itemRepository.findAll()).thenReturn(List.of());

            List<ItemResponse> result = itemService.getAllItems();

            assertTrue(result.isEmpty());
            verify(itemRepository).findAll();
        }
    }

    @Nested
    @DisplayName("getActiveItems")
    class GetActiveItemsTests {

        @Test
        @DisplayName("should return only active items")
        void testGetActiveItems() {
            List<Item> activeItems = List.of(testItem);
            when(itemRepository.findByStatusTrue()).thenReturn(activeItems);

            List<ItemResponse> result = itemService.getActiveItems();

            assertEquals(1, result.size());
            assertTrue(result.get(0).getStatus());
            verify(itemRepository).findByStatusTrue();
            log.info("testGetActiveItems passed: returned {} active item(s)", result.size());
        }

        @Test
        @DisplayName("should return empty list when no active items exist")
        void testGetActiveItemsEmpty() {
            when(itemRepository.findByStatusTrue()).thenReturn(List.of());

            List<ItemResponse> result = itemService.getActiveItems();

            assertTrue(result.isEmpty());
            verify(itemRepository).findByStatusTrue();
        }
    }

    @Nested
    @DisplayName("createItem")
    class CreateItemTests {

        @Test
        @DisplayName("should create item successfully")
        void testCreateItemSuccess() {
            when(itemRepository.save(any(Item.class))).thenReturn(testItem);

            Item result = itemService.createItem(testItem);

            assertEquals(testItem.getId(), result.getId());
            assertEquals("Test Item", result.getName());
            verify(itemRepository).save(testItem);
            log.info("testCreateItemSuccess passed: created item id={}", result.getId());
        }

        @Test
        @DisplayName("should save item with all fields")
        void testCreateItemWithAllFields() {
            when(itemRepository.save(any(Item.class))).thenReturn(testItem);

            Item result = itemService.createItem(testItem);

            assertEquals(99.99, result.getAmount());
            assertEquals(5, result.getQuantity());
            assertEquals("Electronics", result.getCategory().getName());
            verify(itemRepository).save(testItem);
        }
    }

    @Nested
    @DisplayName("updateItem")
    class UpdateItemTests {

        @Test
        @DisplayName("should update item successfully")
        void testUpdateItemSuccess() {
            Item updatedItem = new Item("Updated Item", "Updated Description", 149.99, "http://new.url", 10, testCategory, true);
            updatedItem.setId(1L);

            when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
            when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

            Item result = itemService.updateItem(1L, updatedItem);

            assertEquals("Updated Item", result.getName());
            assertEquals("Updated Description", result.getDescription());
            verify(itemRepository).findById(1L);
            verify(itemRepository).save(any(Item.class));
            log.info("testUpdateItemSuccess passed: updated item name={}", result.getName());
        }

        @Test
        @DisplayName("should update only provided fields")
        void testUpdateItemPartial() {
            Item partialUpdate = new Item();
            partialUpdate.setName("New Name");
            partialUpdate.setAmount(199.99);

            when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
            when(itemRepository.save(any(Item.class))).thenReturn(testItem);

            Item result = itemService.updateItem(1L, partialUpdate);

            assertNotNull(result);
            verify(itemRepository).findById(1L);
            verify(itemRepository).save(any(Item.class));
        }

        @Test
        @DisplayName("should throw exception when item not found")
        void testUpdateItemNotFound() {
            when(itemRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> 
                itemService.updateItem(999L, testItem)
            );
            verify(itemRepository).findById(999L);
            verify(itemRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteItem")
    class DeleteItemTests {

        @Test
        @DisplayName("should delete item successfully")
        void testDeleteItemSuccess() {
            assertDoesNotThrow(() -> itemService.deleteItem(1L));
            verify(itemRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should call repository delete method")
        void testDeleteItemCallsRepository() {
            itemService.deleteItem(5L);
            verify(itemRepository, times(1)).deleteById(5L);
        }
    }
}

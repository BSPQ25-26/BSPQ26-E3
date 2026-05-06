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

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final ProfileRepository profileRepository;

    public ItemService(ItemRepository itemRepository, CategoryRepository categoryRepository, ProfileRepository profileRepository) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems() {
        return itemRepository.findAll().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getActiveItems() {
        return itemRepository.findByStatusTrue().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long id) {
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Item not found"));
        return convertToResponse(item);
    }

    private ItemResponse convertToResponse(Item item) {
        return new ItemResponse(
            item.getId(),
            item.getName(),
            item.getDescription(),
            item.getAmount(),
            item.getImage_URL(),  // ← Esto existe
            item.getQuantity(),
            item.getStatus(),
            item.getCategory() != null ? item.getCategory().getName() : "Uncategorized",
            item.getSeller() != null ? item.getSeller().getId().toString() : null
        );
    }

    public Item createItem(Item item, UUID sellerId) {
        // Obtener el perfil del vendedor
        Profile seller = profileRepository.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("Seller not found"));
        item.setSeller(seller);

        // Si la categoría solo tiene nombre, buscarla en BD
        if (item.getCategory() != null && item.getCategory().getId() == null) {
            String categoryName = item.getCategory().getName();
            Category category = categoryRepository.findByName(categoryName)
                .orElseGet(() -> {
                    // Si no existe, crearla
                    Category newCategory = new Category(categoryName, "");  // ← ¿Existe este constructor?
                    return categoryRepository.save(newCategory);
                });
            item.setCategory(category);
        }

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
package pl.shakhner.PetrushkaProductsBot.services.servicesImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.shakhner.PetrushkaProductsBot.dto.ItemPriceTitleDTO;
import pl.shakhner.PetrushkaProductsBot.exceptions.ItemNotFoundException;
import pl.shakhner.PetrushkaProductsBot.models.Item;
import pl.shakhner.PetrushkaProductsBot.repositories.ItemRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void findByIdReturnsItemWhenItemExists() {
        long itemId = 1;
        Item item = new Item();
        item.setId(itemId);
        item.setTitle("Apple");
        item.setPrice(1.5);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        Item result = itemService.findById(itemId);

        assertEquals(itemId, result.getId());
        assertEquals(item.getTitle(), result.getTitle());
        assertEquals(item.getPrice(), result.getPrice());
    }

    @Test
    void findByIdThrowsItemNotFoundExceptionWhenItemDoesNotExist() {
        long itemId = 1;

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.findById(itemId));
    }

    @Test
    void findItemsPricesByIdReturnsListOfItemWhenItemsExist() {
        long itemId1 = 1;
        long itemId2 = 2;

        Item item1 = new Item();
        item1.setId(itemId1);
        item1.setTitle("Apple");
        item1.setPrice(1.5);

        Item item2 = new Item();
        item2.setId(itemId2);
        item2.setTitle("Banana");
        item2.setPrice(2.0);

        when(itemRepository.findPricesTitlesById(Arrays.asList(itemId1, itemId2))).thenReturn(Arrays.asList(new ItemPriceTitleDTO(1L, 1.5, "Apple"), new ItemPriceTitleDTO(2L, 2.0, "Banana")));

        List<ItemPriceTitleDTO> result = itemService.findItemsPricesById(Arrays.asList(itemId1, itemId2));

        assertEquals(2, result.size());
        assertEquals(itemId1, result.get(0).getId());
        assertEquals("Apple", result.get(0).getTitle());
        assertEquals(1.5, result.get(0).getPrice());
        assertEquals(itemId2, result.get(1).getId());
        assertEquals("Banana", result.get(1).getTitle());
        assertEquals(2.0, result.get(1).getPrice());
    }

    @Test
    void findItemsPricesByIdReturnsEmptyListWhenNoItemsExist() {
        when(itemRepository.findPricesTitlesById(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<ItemPriceTitleDTO> result = itemService.findItemsPricesById(Collections.emptyList());

        assertTrue(result.isEmpty());
    }

    @Test
    void changePriceUpdatesPriceWhenItemExists() {
        long itemId = 1;
        double newPrice = 2.0;

        Item item = new Item();
        item.setId(itemId);
        item.setTitle("Apple");
        item.setPrice(1.5);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        itemService.changePrice(itemId, newPrice);

        assertEquals(newPrice, item.getPrice());
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void changePriceThrowsItemNotFoundExceptionWhenItemDoesNotExist() {
        long itemId = 1;
        double newPrice = 2.0;

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.changePrice(itemId, newPrice));
        verify(itemRepository, never()).save(any());
    }
}

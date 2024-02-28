package pl.shakhner.PetrushkaProductsBot.services;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import pl.shakhner.PetrushkaProductsBot.botAPI.ItemType;
import pl.shakhner.PetrushkaProductsBot.dto.ItemPriceTitleDTO;
import pl.shakhner.PetrushkaProductsBot.models.Item;

import java.util.List;

public interface ItemService {
    public List<Item> findByType(ItemType itemType);

    public Item findById(String id);

    public Item findById(Long id);

    public SendMessage saveItemToDatabase(Update update, ItemType itemType);

    public void deleteItem(Update update);

    public List<ItemPriceTitleDTO> findItemsPricesById(List<Long> itemsId);

    public void changePrice(Long itemId, Double newPrice);

    }

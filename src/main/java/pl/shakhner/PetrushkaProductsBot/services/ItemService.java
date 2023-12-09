package pl.shakhner.PetrushkaProductsBot.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.json.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import pl.shakhner.PetrushkaProductsBot.botAPI.ItemType;
import pl.shakhner.PetrushkaProductsBot.dto.ItemPriceTitleDTO;
import pl.shakhner.PetrushkaProductsBot.exceptions.ItemNotFoundException;
import pl.shakhner.PetrushkaProductsBot.exceptions.ItemNotSaveException;
import pl.shakhner.PetrushkaProductsBot.exceptions.UploadFileException;
import pl.shakhner.PetrushkaProductsBot.models.Item;
import pl.shakhner.PetrushkaProductsBot.repositories.ItemRepository;
import pl.shakhner.PetrushkaProductsBot.util.ButtonUtils;
import pl.shakhner.PetrushkaProductsBot.util.DoubleValidator;
import pl.shakhner.PetrushkaProductsBot.util.LogUtils;
import pl.shakhner.PetrushkaProductsBot.util.MessageUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ItemService {
    private final ItemRepository itemRepository;
    private MessageUtils messageUtils;

    @Value("${bot.token}")
    private String token;

    @Value("${service.file_info.uri}")
    private String fileInfoUri;

    @Value("${service.file_storage.uri}")
    private String fileStorageUri;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<Item> findByType(ItemType itemType){
        List<Item> itemList = itemRepository.findByType(itemType);

        return itemList;
    }

    public Item findById(String id){

        if (id.startsWith("CALLBACK"))
            id = id.replace("CALLBACK_", "");

        Optional<Item> optionalItem = itemRepository.findById(Long.valueOf(id));

        if(optionalItem.isEmpty())
            throw new ItemNotFoundException("Item not found");

        return optionalItem.get();
    }

    public Item findById(Long id){


        Optional<Item> optionalItem = itemRepository.findById(id);

        if(optionalItem.isEmpty())
            throw new ItemNotFoundException("Item not found");

        return optionalItem.get();
    }


    public SendMessage saveItemToDatabase(Update update, ItemType itemType) {
        Message telegramMessage = update.getMessage();

        int photoSizeCount = telegramMessage.getPhoto().size();
        int photoIndex = photoSizeCount > 1 ? telegramMessage.getPhoto().size() - 1 : 0;

        PhotoSize telegramPhoto = telegramMessage.getPhoto().get(photoIndex);

        String fileId = telegramPhoto.getFileId();
        String caption = telegramMessage.getCaption();
        ResponseEntity<String> response = getFilePath(fileId);

        if (response.getStatusCode() == HttpStatus.OK) {

            Item item = getReadyToSaveCard(response, caption, itemType);

            itemRepository.save(item);

            return new SendMessage(String.valueOf(update.getMessage().getChatId()), messageUtils.SUCCESSFULLY_ADDED);
        } else {
            throw new UploadFileException("Bad response from telegram service: " + response);
        }
    }

    private Item getReadyToSaveCard(ResponseEntity<String> response, String caption, ItemType itemType) {
        String filePath = getFilePath(response);
        byte[] fileInByte = downloadFile(filePath);

        switch (itemType) {
            case berry -> caption = caption.replace("/addBerry ", "");
            case fruit -> caption = caption.replace("/addFruit ", "");
            case vegetable -> caption = caption.replace("/addVegetable ", "");
            case herb -> caption = caption.replace("/addHerb ", "");
        }

        String[] dataArray = caption.split("/");

        if (dataArray.length != 3)
            throw new ItemNotSaveException("Incorrect enum");
        Item item = new Item();

        try {
            item.setImage(fileInByte);
            item.setTitle(dataArray[0]);
            item.setDescription(dataArray[1]);
            item.setPrice(DoubleValidator.parseDouble(dataArray[2]));
            item.setType(itemType);
        }catch (Exception e) {
            throw new ItemNotSaveException("Неверно указаны поля");
        }

        return item;
    }

    private byte[] downloadFile(String filePath) {
        String fullUri = fileStorageUri.replace("{token}", token)
                .replace("{filePath}", filePath);
        URL urlObj = null;
        try {
            urlObj = new URL(fullUri);
        } catch (MalformedURLException e) {
            throw new UploadFileException(e.getMessage());
        }

        try (InputStream is = urlObj.openStream()) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new UploadFileException(e.getMessage());
        }
    }

    private ResponseEntity<String> getFilePath(String fileId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        return restTemplate.exchange(
                fileInfoUri,
                HttpMethod.GET,
                request,
                String.class,
                token, fileId
        );
    }

    private String getFilePath(ResponseEntity<String> response) {
        JSONObject jsonObject = new JSONObject(response.getBody());
        return String.valueOf(jsonObject
                .getJSONObject("result")
                .getString("file_path"));
    }

    public void deleteItem(Update update) {
        String callBackId = update.getCallbackQuery().getData().replace(ButtonUtils.DELETE_ITEM_CALLBACK, "");
        itemRepository.deleteById(Long.valueOf(callBackId));
    }

    public List<ItemPriceTitleDTO> findItemsPricesById(List<Long> itemsId) {
        return itemRepository.findPricesTitlesById(itemsId);
    }


    public void changePrice(Long itemId, Double newPrice) {
        Optional<Item> optionalItem = itemRepository.findById(itemId);

        if (optionalItem.isEmpty())
            throw new ItemNotFoundException("Admin trying to delete item, which is not exist in database" + itemId);

        Item item = optionalItem.get();
        item.setPrice(newPrice);

        itemRepository.save(item);
        log.info("Price of the item " + item.getTitle() + " was changed to " + newPrice);
    }
}

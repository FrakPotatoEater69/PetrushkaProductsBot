package pl.shakhner.PetrushkaProductsBot.util;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import pl.shakhner.PetrushkaProductsBot.botAPI.ItemType;
import pl.shakhner.PetrushkaProductsBot.botAPI.Unit;
import pl.shakhner.PetrushkaProductsBot.models.Item;
import pl.shakhner.PetrushkaProductsBot.services.ItemService;

import java.util.ArrayList;
import java.util.List;

@Component
public class KeyboardUtils {

    private final ButtonUtils buttonUtils;
    private final AdminValidator adminValidator;
    private final ItemService itemService;

    public KeyboardUtils(ButtonUtils buttonUtils, AdminValidator adminValidator, ItemService itemService) {
        this.buttonUtils = buttonUtils;
        this.adminValidator = adminValidator;
        this.itemService = itemService;
    }

    public ReplyKeyboardMarkup getMainKeyboard(){
        ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow keyboardrow = new KeyboardRow();

        keyboardrow.add("Каталог");
        keyboardrow.add("Моя корзина");

        rows.add(keyboardrow);

        keyboardrow = new KeyboardRow();

        keyboardrow.add("Информация и помощь");
        keyboardrow.add("Абсолютно бесполезная кнопка");

        rows.add(keyboardrow);

        replyKeyboard.setResizeKeyboard(true);
        replyKeyboard.setKeyboard(rows);

        return replyKeyboard;
    }

    public InlineKeyboardMarkup getFruitsOrVegetablesKeyboard(Update update) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        rowInLine.add(getButton(ButtonUtils.FRUITS_BUTTON, ButtonUtils.FRUITS_CALLBACK));

        rowInLine.add(getButton(ButtonUtils.VEGETABLES_BUTTON, ButtonUtils.VEGETABLES_CALLBACK));

        rowsInLine.add(rowInLine);

        rowInLine = new ArrayList<>();

        rowInLine.add(getButton(ButtonUtils.HERB_BUTTON, ButtonUtils.HERB_CALLBACK));

        rowInLine.add(getButton(ButtonUtils.BERRY_BUTTON, ButtonUtils.BERRY_CALLBACK));

        rowsInLine.add(rowInLine);

        inlineKeyboardMarkup.setKeyboard(rowsInLine);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getSubMenuKeyboard(String type) {
        List<Item> itemList = itemService.findByType(ItemType.valueOf(type));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        InlineKeyboardButton button;

        for (int i = 0; i < itemList.size(); i++) {
            Item item = itemList.get(i);
            String buttonText = item.getTitle();

            button = new InlineKeyboardButton();
            button.setText(buttonText);
            button.setCallbackData("CALLBACK_" + item.getId());

            rowInLine.add(button);

            if (rowInLine.size() == 2 || i == itemList.size() - 1) {
                rowsInLine.add(rowInLine);
                rowInLine = new ArrayList<>();
            }
        }

        rowInLine = new ArrayList<>();

        rowInLine.add(getButton(ButtonUtils.BACK_TEXT, ButtonUtils.BACK_FROM_SUB_CATALOG + type));

        rowsInLine.add(rowInLine);

        inlineKeyboardMarkup.setKeyboard(rowsInLine);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getPersonalItemKeyboard(String itemId, String type, Long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        rowInLine.add(getButton(ButtonUtils.BACK_TEXT, ButtonUtils.BACK_FROM_PERSONAL_PAGE + type));

        rowInLine.add(getButton(buttonUtils.ADD_TO_CART_BUTTON, buttonUtils.ADD_TO_CART_CALLBACK + itemId));

        rowsInLine.add(rowInLine);

        if(adminValidator.isOwner(chatId)){
            rowInLine = new ArrayList<>();

            rowInLine.add(getButton(ButtonUtils.DELETE_ITEM_TEXT, ButtonUtils.DELETE_ITEM_CALLBACK + itemId));

            rowInLine.add(getButton(ButtonUtils.CHANGE_PRICE_TEXT, ButtonUtils.CHANGE_PRICE_CALLBACK + itemId));

            rowsInLine.add(rowInLine);
        }

        inlineKeyboardMarkup.setKeyboard(rowsInLine);

        return inlineKeyboardMarkup;
    }

    public ReplyKeyboard getChooseUnitKeyboard(Long itemId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        rowInLine.add(getButton(ButtonUtils.KG_TEXT, Unit.kg.toString() + itemId));

        rowInLine.add(getButton(ButtonUtils.PCS_TEXT, Unit.pcs.toString() + itemId));

        rowsInLine.add(rowInLine);
        inlineKeyboardMarkup.setKeyboard(rowsInLine);

        return inlineKeyboardMarkup;
    }

    public ReplyKeyboard getCartKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        rowInLine.add(getButton(ButtonUtils.DELETE_ITEM_FROM_CART_TEXT, ButtonUtils.DELETE_FROM_CART_CALLBACK));

        rowInLine.add(getButton(ButtonUtils.CLEAR_CART_TEXT, ButtonUtils.CLEAR_CART_CALLBACK));

        rowsInLine.add(rowInLine);

        rowInLine = new ArrayList<>();

        rowInLine.add(getButton(ButtonUtils.CREATE_ORDER_TEXT, ButtonUtils.CREATE_ORDER_CALLBACK));
        rowsInLine.add(rowInLine);

        inlineKeyboardMarkup.setKeyboard(rowsInLine);

        return inlineKeyboardMarkup;
    }

    private InlineKeyboardButton getButton(String text, String callback){
        InlineKeyboardButton button;

        button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callback);

        return button;
    }
}

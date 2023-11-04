package pl.shakhner.PetrushkaProductsBot.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import pl.shakhner.PetrushkaProductsBot.botAPI.ItemInCart;
import pl.shakhner.PetrushkaProductsBot.botAPI.UserCartData;
import pl.shakhner.PetrushkaProductsBot.cache.UserDataCache;
import pl.shakhner.PetrushkaProductsBot.dto.ItemPriceTitleDTO;
import pl.shakhner.PetrushkaProductsBot.exceptions.CartIsEmptyException;
import pl.shakhner.PetrushkaProductsBot.exceptions.ItemNotFoundException;
import pl.shakhner.PetrushkaProductsBot.models.Item;
import pl.shakhner.PetrushkaProductsBot.services.ItemService;

import java.io.ByteArrayInputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MessageUtils {
    public static final String HELP_MESSAGE = "Если что-то идёт не так - просто отправь мне отмена, или введи /developer и свяжись с разработчиком";
    public static final String DEVELOPER_HELP = "Если у тебя возникли неразрешимые проблемы - напиши мне @luv_scars_KO";
    public static final String INFO = "Привет, я - бот Petrushka Products! \uD83D\uDED2 Я помогу тебе выбрать свежие овощи, фрукты и другие продукты, а затем доставлю их прямо к твоей двери \uD83D\uDE9A\uD83D\uDCA8 Просто выбирай товары, добавляй их в корзину и делай заказ. Отправь мне /help если что-то идёт не так.\n" +
            "\n" +
            "Важно знать, что я оборудован системой антиспама. Это означает, что если ты отправляешь сообщения слишком часто (больше одного в секунду), я не буду на них отвечать. Помни об этом!\n" +
            "\n" +
            "Для начала работы просто отправь мне 'Каталог' или используй кнопки внизу. Приятного выбора и вкусных покупок! \uD83E\uDD66\uD83C\uDF4E\uD83D\uDED2" +
            "\n" +
            "Помни, что корзина пользоваталей, для экономим места, обновляется каждую полночь, так что не затягивай с покупками";
    public static final String MAILING_WAS_ENDED = "Рассылка закончена, кол-во пользователей, получивших сообщение: ";
    @Value("${manager.number}")
    private String managerNumber;
    public static final String SUCCESSFULLY_ADDED = "Успешно добавлено";
    public static final String SUCCESSFULLY_DELETED = "Товар успешно удалён";
    public static final String UNSUPPORTED_COMMAND = "Такой команды не существует, воспользуйтесь меню";
    public static final String ADMIN_WARNING = "Чтобы добавить новый продукт, отправьте фотографию продукта, НЕ СЖИМАЯ, в описании картинки впиши команду /addFruit , /addHerd , /addVegetable , /addBerry " +
            "в зависимости от типа продукта, и НАЗВАНИЕ/ОПИСАНИЕ/ЦЕНА" +
            "\n" +
            "Пример:\n" +
            "/addFruit Банан/Вкусный банан/4.39";
    public static final String ITEM_NOT_FOUND_WARNING = "Этот товар не существует, скорее всего, он был удалён";
    public static final String END_EVERY_ORDER_WARNING = "Пожалуйста, заканчивай каждое добавление в корзину до конца.";
    public static final String ENTER_KG_AMOUNT = "Введи количество товара в килограммах\nИли введи отмена";
    public static final String ENTER_PCS_AMOUNT = "Введи количество товара в штуках\nИли введи отмена";
    public static final String INCORRECT_DIGIT_INPUT_WARNING = "Некорректный ввод числа, пример:\n2.2\n2,2\n2\nИли введите отмена";
    public static final String CANCEL_ORDER = "Люда! Неси отмену!";
    public static final String NOTHING_TO_CANCEL = "Я зря Люду звать не буду, нечего отменять";
    public static final String CART_IS_EMPTY = "Твоя корзина пуста, пора что-то купить!";
    public static final String INCORRECT_ROUND_DIGIT_INPUT_WARNING = "Поштучно товар можно заказать только целым, введи пожалуйста количество";
    public static final String SUCCESSFULLY_ADDED_TO_CART = "Товар добавлен в корзину";
    public static final String INCOMPATIBLE_UNITS = "У тебя в корзине уже есть этот товар, но в других единицах измерения.\nПожалуйста, выбери что-то одно";
    public static final String DELETE_ITEM_FROM_CART_MESSAGE = "Введи порядковый номер (цифра слева) продукта";
    public static final String INCORRECT_ORDINAL_NUMBER_INPUT_WARNING = "Введите порядковый номер товара в корзине или введите отмена";
    public static final String SUCCESSFULLY_DELETED_FROM_CART = "Принято, удалил";
    public static final String CART_DOES_NOT_CONTAIN_SUCH_A_ORDINAL_NUMBER = "У тебя в корзине нет такого товара\nВведи номер товара из корзины или введи отмена";
    public static final String INSERT_NEW_AMOUNT = "Введи новую цену через точку или запятую, для отмены нажми отмена";
    public static final String INCORRECT_PRICE_INPUT_WARNING = "Цена введена неверно, пожалуйста, введи новую цену товара, пример:\n 5.1\n12,1\n7";
    public static final String CANCEL_ADMIN = "Изменение цены отменено";
    public static final String PRICE_SUCCESSFULLY_CHANGED = "Цена товара изменена";
    public static final String INSERT_MOBILE_NUMBER = "Введите свой мобильный номер в формате:\n+375(29,44,25)7777777";
    public static final String NUMBER_ACCEPTED_WAIT_FOR_ADDRESS = "Теперь введи свой адрес";
    public static final String FINISH_ORDER_WARNING = "Пожалуйста, закончите оформление заказа или введите отмена";
    public static final String WRONG_NUMBER_INPUT = "Ты неправильно ввёл номер, вот пример:\n+375446667722\n+375296665544\n+375256667744\nИли введи отмена, чтобы отменить заказ";
    public static final String ORDER_CANCELLED = "Заказ отменён";
    public static final String ADDRESS_ACCEPTED = "Адрес принят, доставим в лучше виде";
    public String ORDER_LIMIT_WARNING;
    public String CART_LIMIT_WARNING;
    private static final String FRUIT_MENU = "Меню фруктов";
    private static final String VEGETABLE_MENU = "Меню овощей";
    private static final String HERB_MENU = "Меню трав";
    private static final String BERRY_MENU = "Меню ягод";
    private static final String CHOOSE_UNIT = "Как хочешь сделать заказ?\nВ килограммах или поштучно?\n\nДля отмены отправь мне Отмена";


    private final ItemService itemService;
    private final Extractor chatIdExtractor;
    private final UserDataCache userDataCache;

    public MessageUtils(ItemService itemService, Extractor chatIdExtractor, UserDataCache userDataCache, KeyboardUtils keyboardUtils) {
        this.itemService = itemService;
        this.chatIdExtractor = chatIdExtractor;
        this.userDataCache = userDataCache;
        this.keyboardUtils = keyboardUtils;

        ORDER_LIMIT_WARNING = "Заказать больше 15 килограмм или 100 штук одного продукта можно только по номеру: " + managerNumber;
    }

    @PostConstruct
    private void warnings(){
        ORDER_LIMIT_WARNING = "Заказать больше 15 килограмм или 100 штук одного продукта можно только по номеру: " + managerNumber;
        CART_LIMIT_WARNING = "Можно заказать только 15 товаров за 1 заказ, для заказа большего количества товаров позвоните по номеру: " + managerNumber;

    }

    public static final String WELCOME_MESSAGE = "Ола, Амигос!\nЗдесь мы не только предлагаем самые свежие продукты, но и доставляем их прямо к вашей двери. Теперь вы можете запастись витаминами, не выходя из дома!\n" +
            "\n" +
            "Наша команда работает усердно, чтобы вы получали только лучшее. Наши овощи и фрукты такие свежие, что они могут сказать вам последние новости из огорода!" +
            "\n для начала работы напиши каталог или воспользуйся кнопками снизу";
    private static final String CHOOSE_FRUITS_OR_VEGETABLES_TEXT = "Выбери от чего будешь кайфовать";

    @Value("${main.menu.picture}")
    private String mainMenuPicFileId;

    @Value("${berry.subCatalog.picture}")
    private String berrySubCatalogPicFileId;

    @Value("${vegetable.subCatalog.picture}")
    private String vegetableSubCatalogPicFileId;

    @Value("${fruit.subCatalog.picture}")
    private String fruitSubCatalogPicFileId;

    @Value("${herb.subCatalog.picture}")
    private String herbSubCatalogPicFileId;
    private final KeyboardUtils keyboardUtils;

    public SendMessage generateSendMessage(Update update, String text) {
        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(chatIdExtractor.extractChatIdFromUpdate(update));

        sendMessage.setText(text);

        return sendMessage;
    }

    public SendPhoto generateMainCatalog(Update update) {
        SendPhoto sendPhoto = generateSendPhoto(update, CHOOSE_FRUITS_OR_VEGETABLES_TEXT);
        sendPhoto.setPhoto(new InputFile(mainMenuPicFileId));
        sendPhoto.setReplyMarkup(keyboardUtils.getFruitsOrVegetablesKeyboard(update));
        return sendPhoto;
    }

    private SendPhoto generateSendPhoto(Update update, String caption) {
        SendPhoto sendPhoto = new SendPhoto();

        sendPhoto.setChatId(chatIdExtractor.extractChatIdFromUpdate(update));

        sendPhoto.setCaption(caption);

        return sendPhoto;
    }

    public EditMessageCaption getSubCatalog(Update update) {
        EditMessageCaption editMessageCaption = new EditMessageCaption();
        enrichEditMessageWithSubMenu(editMessageCaption, update);
        return editMessageCaption;
    }

    private void enrichEditMessageWithSubMenu(EditMessageCaption editMessageCaption, Update update) {
        String caption = null;
        String callBackData = update.getCallbackQuery().getData();

        if(update.getCallbackQuery().getData().startsWith("Back_P"))
            callBackData = callBackData.replace("Back_P_","");

        switch (callBackData){
            case "fruit":
                caption = FRUIT_MENU;
                break;
            case "vegetable":
                caption = VEGETABLE_MENU;
                break;
            case "herb":
                caption = HERB_MENU;
                break;
            case "berry":
                caption = BERRY_MENU;
                break;
        }
        editMessageCaption.setCaption(caption);
        editMessageCaption.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        editMessageCaption.setChatId(chatIdExtractor.extractChatIdFromUpdate(update));
        editMessageCaption.setReplyMarkup(keyboardUtils.getSubMenuKeyboard(callBackData));
    }

    public EditMessageCaption getPersonalItemCaption(Update update) {
        String itemId = update.getCallbackQuery().getData().replace("CALLBACK_", "");
        Item item = null;

        try {
            item = itemService.findById(itemId);
        }catch (ItemNotFoundException e){
            log.error("User " + update.getCallbackQuery().getFrom().getFirstName() + update.getCallbackQuery().getMessage().getChatId() + " send callback" + update.getCallbackQuery().getData() + "and lead to error"  + e.getMessage());
        }

        EditMessageCaption editMessageCaption = new EditMessageCaption();
        editMessageCaption.setCaption(getPersonalItemText(item));
        editMessageCaption.setChatId(chatIdExtractor.extractChatIdFromUpdate(update));
        editMessageCaption.setMessageId(update.getCallbackQuery().getMessage().getMessageId());

        editMessageCaption.setReplyMarkup(keyboardUtils.getPersonalItemKeyboard(itemId, item.getType().toString(), update.getCallbackQuery().getMessage().getChat().getId()));
        //TODO
        return editMessageCaption;
    }

    public EditMessageMedia getPersonalItemPicture(Update update){
        String itemId = update.getCallbackQuery().getData().replace("CALLBACK_", "");
        Item item = null;

        try {
            item = itemService.findById(itemId);
        }catch (ItemNotFoundException e){
            log.error("User " + update.getCallbackQuery().getFrom().getFirstName() + update.getCallbackQuery().getMessage().getChatId() + " send callback" + update.getCallbackQuery().getData() + "and lead to error"  + e.getMessage());
        }

        EditMessageMedia editMessageMedia = new EditMessageMedia();
        editMessageMedia.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        editMessageMedia.setChatId(chatIdExtractor.extractChatIdFromUpdate(update));

        InputMediaPhoto inputMediaPhoto = new InputMediaPhoto();
        inputMediaPhoto.setMedia(new ByteArrayInputStream(item.getImage()), "item");
        editMessageMedia.setMedia(inputMediaPhoto);


        return editMessageMedia;
    }

    private String getPersonalItemText(Item item){
        String personalText = item.getTitle() + "\n\n" + item.getDescription() + "\n\n" + "Цена: " + item.getPrice() + " рублей за кг";

        return personalText;
    }
    public EditMessageCaption getMainCatalogCapture(Update update) {
        EditMessageCaption editMessageCaption = new EditMessageCaption();
        editMessageCaption.setReplyMarkup(keyboardUtils.getFruitsOrVegetablesKeyboard(update));
        editMessageCaption.setChatId(chatIdExtractor.extractChatIdFromUpdate(update));
        editMessageCaption.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        editMessageCaption.setCaption(CHOOSE_FRUITS_OR_VEGETABLES_TEXT);

        return editMessageCaption;
    }

    public EditMessageMedia getMainCatalogPicture(Update update) {
        EditMessageMedia editMessageMedia = new EditMessageMedia();
        editMessageMedia.setMedia(new InputMediaPhoto(mainMenuPicFileId));
        editMessageMedia.setChatId(chatIdExtractor.extractChatIdFromUpdate(update));
        editMessageMedia.setMessageId(update.getCallbackQuery().getMessage().getMessageId());

        return editMessageMedia;
    }

    public EditMessageMedia getSubCatalogPicture(Update update) {
        EditMessageMedia editMessageMedia = new EditMessageMedia();
        editMessageMedia.setChatId(chatIdExtractor.extractChatIdFromUpdate(update));
        editMessageMedia.setMessageId(update.getCallbackQuery().getMessage().getMessageId());

        String callbackData = update.getCallbackQuery().getData();

        if(callbackData.startsWith(ButtonUtils.BACK_FROM_PERSONAL_PAGE))
            callbackData = callbackData.replace(ButtonUtils.BACK_FROM_PERSONAL_PAGE, "");

        InputMediaPhoto inputMediaPhoto = new InputMediaPhoto();

        switch (callbackData){
            case ButtonUtils.FRUITS_CALLBACK:
                inputMediaPhoto.setMedia(fruitSubCatalogPicFileId);
                break;
            case ButtonUtils.VEGETABLES_CALLBACK:
                inputMediaPhoto.setMedia(vegetableSubCatalogPicFileId);
                break;
            case ButtonUtils.BERRY_CALLBACK:
                inputMediaPhoto.setMedia(berrySubCatalogPicFileId);
                break;
            case ButtonUtils.HERB_CALLBACK:
                inputMediaPhoto.setMedia(herbSubCatalogPicFileId);
                break;
        }

        editMessageMedia.setMedia(inputMediaPhoto);
        return editMessageMedia;
    }

    public SendMessage chooseUnit(Update update, Long itemId) {
        SendMessage sendMessage = generateSendMessage(update, CHOOSE_UNIT);
        sendMessage.setReplyMarkup(keyboardUtils.getChooseUnitKeyboard(itemId));

        return sendMessage;
    }

    public DeleteMessage generateDeleteMessage(Update update) {
        DeleteMessage deleteMessage = new DeleteMessage();

        Integer messageId;

        if(update.hasCallbackQuery()){
            messageId = update.getCallbackQuery().getMessage().getMessageId();
        }else {
            messageId = update.getMessage().getMessageId();
        }

        deleteMessage.setMessageId(messageId);
        deleteMessage.setChatId(chatIdExtractor.extractChatIdFromUpdate(update));

        return deleteMessage;
    }


    public String generateUserCart(Update update) {
        UserCartData userCartData = userDataCache.getUserCart().get(chatIdExtractor.extractChatIdFromUpdate(update));

        if (userCartData == null)
            throw new CartIsEmptyException(ExceptionUtils.cartIsEmptyExceptionBuilder(update));

        Map<Long, ItemInCart> userCart = userCartData.getItemsInCart();

        if (userCart == null || userCart.isEmpty())
            throw new CartIsEmptyException(ExceptionUtils.cartIsEmptyExceptionBuilder(update));

        List<ItemPriceTitleDTO> itemList = itemService.findItemsPricesById(userCart.keySet().stream().toList());

        Comparator<ItemPriceTitleDTO> byOrdinalNumber = Comparator.comparing(itemDTO -> userCartData.getOrdinalItemNumberItemId().get(itemDTO.getId()));

        itemList.sort(byOrdinalNumber);

        StringBuilder sb = new StringBuilder();

        for (ItemPriceTitleDTO itemDTO : itemList) {
            ItemInCart itemInCart = userCart.get(itemDTO.getId());
            sb.append(userCartData.getOrdinalItemNumberItemId().get(itemDTO.getId())).append(". Товар: ").append(itemDTO.getTitle()).append(" ").append("Цена: ").append(itemDTO.getPrice()).append(" рублей за кг, ").
                    append("Кол-во: ").append(itemInCart.getAmount()).append(" ").append(itemInCart.getUnit().getTitle()).append("\n");
        }

        return sb.toString();
    }

    public String generateUserCart(UserCartData userCartData){
        Map<Long, ItemInCart> userCart = userCartData.getItemsInCart();

        List<ItemPriceTitleDTO> itemList = itemService.findItemsPricesById(userCart.keySet().stream().toList());

        Comparator<ItemPriceTitleDTO> byOrdinalNumber = Comparator.comparing(itemDTO -> userCartData.getOrdinalItemNumberItemId().get(itemDTO.getId()));

        itemList.sort(byOrdinalNumber);

        StringBuilder sb = new StringBuilder();

        for (ItemPriceTitleDTO itemDTO : itemList) {
            ItemInCart itemInCart = userCart.get(itemDTO.getId());
            sb.append(userCartData.getOrdinalItemNumberItemId().get(itemDTO.getId())).append(". Товар: ").append(itemDTO.getTitle()).append(" ").append("Цена: ").append(itemDTO.getPrice()).append(" рублей за кг, ").
                    append("Кол-во: ").append(itemInCart.getAmount()).append(" ").append(itemInCart.getUnit().getTitle()).append("\n");
        }

        sb.append("номер: ").append(userCartData.getNumber()).append("\nадрес: ").append(userCartData.getAddress());

        return sb.toString();
    }

}


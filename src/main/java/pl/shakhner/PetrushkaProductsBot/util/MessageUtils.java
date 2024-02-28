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
import pl.shakhner.PetrushkaProductsBot.services.servicesImpl.ItemServiceImpl;

import java.io.ByteArrayInputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MessageUtils {
    public static final String HELP_MESSAGE = "If something goes wrong, just send me 'cancel', or type /developer and contact the developer.";
    public static final String DEVELOPER_HELP = "If you have unresolved issues, write to me @luv_scars_KO.";
    public static final String INFO = "Hello, I am Petrushka Products bot! \uD83D\uDED2 I will help you choose fresh vegetables, fruits, and other products, and then deliver them right to your door \uD83D\uDE9A\uD83D\uDCA8 Just select items, add them to your cart, and place an order. Send me /help if something goes wrong.\n" +
            "\n" +
            "It's important to know that I have an anti-spam system. This means that if you send messages too frequently (more than one per second), I won't respond to them. Keep that in mind!\n" +
            "\n" +
            "To get started, just send me 'Catalog' or use the buttons below. Enjoy your shopping and tasty choices! \uD83E\uDD66\uD83C\uDF4E\uD83D\uDED2" +
            "\n" +
            "Remember that the user's cart is updated every midnight to save space, so don't delay your purchases.";
    public static final String MAILING_WAS_ENDED = "Mailing has ended, number of users who received the message: ";
    @Value("${manager.number}")
    private String managerNumber;
    public static final String SUCCESSFULLY_ADDED = "Successfully added";
    public static final String SUCCESSFULLY_DELETED = "Product successfully deleted";
    public static final String UNSUPPORTED_COMMAND = "This command does not exist, use the menu.";
    public static final String ADMIN_WARNING = "To add a new product, send a photo of the product, WITHOUT COMPRESSING it, in the image description, include the command /addFruit, /addHerd, /addVegetable, /addBerry, depending on the product type, and NAME/DESCRIPTION/PRICE" +
            "\n" +
            "Example:\n" +
            "/addFruit Banana/Delicious banana/4.39";
    public static final String ITEM_NOT_FOUND_WARNING = "This product does not exist, it was most likely deleted.";
    public static final String END_EVERY_ORDER_WARNING = "Please finish each cart addition completely.";
    public static final String ENTER_KG_AMOUNT = "Enter the quantity of the product in kilograms\nOr enter 'cancel'.";
    public static final String ENTER_PCS_AMOUNT = "Enter the quantity of the product in pieces\nOr enter 'cancel'.";
    public static final String INCORRECT_DIGIT_INPUT_WARNING = "Incorrect number input, example:\n2.2\n2,2\n2\nOr enter 'cancel'.";
    public static final String CANCEL_ORDER = "Luda! Bring the cancellation!";
    public static final String NOTHING_TO_CANCEL = "I won't call Luda in vain, there's nothing to cancel.";
    public static final String CART_IS_EMPTY = "Your cart is empty, it's time to buy something!";
    public static final String INCORRECT_ROUND_DIGIT_INPUT_WARNING = "You can only order whole items, please enter the quantity.";
    public static final String SUCCESSFULLY_ADDED_TO_CART = "Product added to the cart.";
    public static final String INCOMPATIBLE_UNITS = "You already have this product in your cart, but in different units of measurement.\nPlease choose one.";
    public static final String DELETE_ITEM_FROM_CART_MESSAGE = "Enter the ordinal number (number on the left) of the product.";
    public static final String INCORRECT_ORDINAL_NUMBER_INPUT_WARNING = "Enter the ordinal number of the product in the cart or enter 'cancel'.";
    public static final String SUCCESSFULLY_DELETED_FROM_CART = "Accepted, deleted.";
    public static final String CART_DOES_NOT_CONTAIN_SUCH_A_ORDINAL_NUMBER = "You don't have such a product in your cart.\nEnter the product number from the cart or enter 'cancel'.";
    public static final String INSERT_NEW_AMOUNT = "Enter a new price using a dot or comma, to cancel press 'cancel'.";
    public static final String INCORRECT_PRICE_INPUT_WARNING = "Price entered incorrectly, please enter the new price of the product, for example:\n 5.1\n12,1\n7";
    public static final String CANCEL_ADMIN = "Price change canceled.";
    public static final String PRICE_SUCCESSFULLY_CHANGED = "Product price changed.";
    public static final String INSERT_MOBILE_NUMBER = "Enter your mobile number in the format:\n+375(29,44,25)7777777";
    public static final String NUMBER_ACCEPTED_WAIT_FOR_ADDRESS = "Now enter your address.";
    public static final String FINISH_ORDER_WARNING = "Please complete the order or enter 'cancel'.";
    public static final String WRONG_NUMBER_INPUT = "You entered the number incorrectly, here is an example:\n+375446667722\n+375296665544\n+375256667744\nOr enter 'cancel' to cancel the order.";
    public static final String ORDER_CANCELLED = "Order canceled.";
    public static final String ADDRESS_ACCEPTED = "Address accepted, we will deliver it in the best condition.";
    public String ORDER_LIMIT_WARNING;
    public String CART_LIMIT_WARNING;
    private static final String FRUIT_MENU = "Fruit menu";
    private static final String VEGETABLE_MENU = "Vegetable menu";
    private static final String HERB_MENU = "Herb menu";
    private static final String BERRY_MENU = "Berry menu";
    private static final String CHOOSE_UNIT = "How do you want to place an order?\nIn kilograms or per piece?\n\nTo cancel, send me 'Cancel'.";


    private final ItemServiceImpl itemService;
    private final Extractor chatIdExtractor;
    private final UserDataCache userDataCache;

    public MessageUtils(ItemServiceImpl itemService, Extractor chatIdExtractor, UserDataCache userDataCache, KeyboardUtils keyboardUtils) {
        this.itemService = itemService;
        this.chatIdExtractor = chatIdExtractor;
        this.userDataCache = userDataCache;
        this.keyboardUtils = keyboardUtils;

        ORDER_LIMIT_WARNING = "You can order more than 15 kilograms or 100 pieces of the same product only by calling: " + managerNumber;
    }

    @PostConstruct
    private void warnings(){
        ORDER_LIMIT_WARNING = "You can order more than 15 kilograms or 100 pieces of the same product only by calling: " + managerNumber;
        CART_LIMIT_WARNING = "You can order only 15 items in one order, to order more items, call the following number: " + managerNumber;
    }

    public static final String WELCOME_MESSAGE = "Hello, Amigos!\nHere, we not only offer the freshest products but also deliver them right to your door. Now you can stock up on vitamins without leaving your home!\n" +
            "\n" +
            "Our team works diligently to ensure that you receive only the best. Our vegetables and fruits are so fresh that they can tell you the latest news from the garden!" +
            "\nTo get started, type 'catalog' or use the buttons below.";
    private static final String CHOOSE_FRUITS_OR_VEGETABLES_TEXT = "Choose what you'll enjoy";

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
        sendPhoto.setReplyMarkup(keyboardUtils.getFruitsOrVegetablesKeyboard());
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
        String personalText = item.getTitle() + "\n\n" + item.getDescription() + "\n\n" + "Price: " + item.getPrice() + " $ for kg";

        return personalText;
    }
    public EditMessageCaption getMainCatalogCapture(Update update) {
        EditMessageCaption editMessageCaption = new EditMessageCaption();
        editMessageCaption.setReplyMarkup(keyboardUtils.getFruitsOrVegetablesKeyboard());
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
            sb.append(userCartData.getOrdinalItemNumberItemId().get(itemDTO.getId())).append(". Item: ").append(itemDTO.getTitle()).append(" ").append("Price: ").append(itemDTO.getPrice()).append(" $ for kg, ").
                    append("qty: ").append(itemInCart.getAmount()).append(" ").append(itemInCart.getUnit().getTitle()).append("\n");
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
            sb.append(userCartData.getOrdinalItemNumberItemId().get(itemDTO.getId())).append(". Item: ").append(itemDTO.getTitle()).append(" ").append("Price: ").append(itemDTO.getPrice()).append(" $ for kg, ").
                    append("qty: ").append(itemInCart.getAmount()).append(" ").append(itemInCart.getUnit().getTitle()).append("\n");
        }

        sb.append("Number: ").append(userCartData.getNumber()).append("\naddress: ").append(userCartData.getAddress());

        return sb.toString();
    }

}


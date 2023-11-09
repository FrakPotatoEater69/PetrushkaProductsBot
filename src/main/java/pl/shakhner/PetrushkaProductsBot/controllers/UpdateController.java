package pl.shakhner.PetrushkaProductsBot.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pl.shakhner.PetrushkaProductsBot.botAPI.BotState;
import pl.shakhner.PetrushkaProductsBot.botAPI.ItemType;
import pl.shakhner.PetrushkaProductsBot.botAPI.Unit;
import pl.shakhner.PetrushkaProductsBot.botAPI.UserCartData;
import pl.shakhner.PetrushkaProductsBot.cache.AdminDataCache;
import pl.shakhner.PetrushkaProductsBot.cache.UserDataCache;
import pl.shakhner.PetrushkaProductsBot.exceptions.*;
import pl.shakhner.PetrushkaProductsBot.models.User;
import pl.shakhner.PetrushkaProductsBot.services.ItemService;
import pl.shakhner.PetrushkaProductsBot.services.UserService;
import pl.shakhner.PetrushkaProductsBot.util.*;

import java.util.List;

@Controller
@Slf4j
public class UpdateController {
    private TelegramBot telegramBot;
    private final MessageUtils messageUtils;

    private final KeyboardUtils keyboardUtils;

    private final UserDataCache userDataCache;
    private final AdminValidator adminValidator;
    private final ItemService itemService;
    private final LogUtils logUtils;
    private final AdminDataCache adminDataCache;
    private final UserService userService;

    public UpdateController(MessageUtils messageUtils, KeyboardUtils keyboardUtils, UserDataCache userDataCache, AdminValidator adminValidator, ItemService itemService, LogUtils logUtils, Extractor chatIdExtractor, AdminDataCache adminDataCache, UserService userService) {
        this.messageUtils = messageUtils;
        this.keyboardUtils = keyboardUtils;
        this.userDataCache = userDataCache;
        this.adminValidator = adminValidator;
        this.itemService = itemService;
        this.logUtils = logUtils;
        this.adminDataCache = adminDataCache;
        this.userService = userService;
    }

    public void registerBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Received update is null");
            return;
        }if((update.hasMessage() && update.getMessage().hasText()) && (update.getMessage().getText().equals("/help") || update.getMessage().getText().equals("/developer"))){
            if(RateLimiter.isRequestAllowed(String.valueOf(Extractor.extractChatIdFromUpdate(update)))) {
                helpRequestReceived(update);
                log.info("User: " + Extractor.extractFirstnameFromUpdate(update) + " with chatId: " + Extractor.extractChatIdFromUpdate(update) + " send help request");
            }
            return;
        }
        if(userDataCache.isUserInOrderStatus(update)){
            if(RateLimiter.isRequestAllowed(String.valueOf(Extractor.extractChatIdFromUpdate(update)))) {
                log.info("process ad order for user" + Extractor.extractChatIdFromUpdate(update));
                if (update.hasMessage() && update.getMessage().isUserMessage())
                    processMessageInOrderStatus(update);
                else
                    sendFinishOrderWarning(update);
            }
            return;
        }
        if (update.hasCallbackQuery() && update.getCallbackQuery().getMessage().isUserMessage()) {
            processCallBackQuery(update);
            return;
        }
        if (update.getMessage() == null) {
            log.error("Received message is null" + update);
            return;
        }
        if (!update.getMessage().isUserMessage()) {
            log.warn("Bot get massage not from user chat");
            return;
        }
        if(update.getMessage().hasPhoto()){
            if(adminValidator.isOwner(update))
                processMessage(update);
            else
                unsupportedMessageReceived(update);
            return;
        }
        if (!RateLimiter.isRequestAllowed(String.valueOf(Extractor.extractChatIdFromUpdate(update)))) {
            log.warn("User " + update.getMessage().getChat().getFirstName() + " with chatId: " + Extractor.extractChatIdFromUpdate(update) + " is spamming!");
        } else {
            processMessage(update);
        }
    }

    private void processMessage(Update update) {
        if (update.getMessage().hasText()) {
            processTextMessage(update);
        } else if (update.getMessage().hasPhoto()) {
            processPhotoMessage(update);
        } else if (update.getMessage().hasVideo()) {
        }
    }

    private void processTextMessage(Update update) {
        String messageText = update.getMessage().getText();
        if(userDataCache.getCurrentUserBotState(update).equals(BotState.WAIT_FOR_AMOUNT)) {
            processAmountMessage(update, messageText);
        } else if (userDataCache.getCurrentUserBotState(update).equals(BotState.WAIT_FOR_ORDINAL_NUMBER_FOR_DELETING)) {
            processOrdinalNumber(update, messageText);
        } else if (userDataCache.getCurrentUserBotState(update).equals(BotState.WAIT_FOR_NEW_PRICE)){
            processNewPrice(update);
        }else if(messageText.startsWith("/sendAll")) {
            mailingRequestReceived(update);
        } else {
            switch (messageText) {
                    case "/start" -> registerUser(update);
                    case "Catalog", "catalog" -> sendCatalogToUser(update);
                    case "cancel", "Cancel" -> cancelOrder(update);
                    case "My cart" -> sendCartToUser(update);
                    case "Info and help" -> infoRequestReceived(update);
                    default -> unsupportedMessageReceived(update);
            }
        }
    }

    private void processPhotoMessage(Update update) {
        String caption = update.getMessage().getCaption();
        if(caption == null)
            sendAdminWarning(update);
        else if(caption.startsWith("/addBerry"))
            addNewItemToDatabase(update, ItemType.berry);
        else if (caption.startsWith("/addFruit"))
            addNewItemToDatabase(update, ItemType.fruit);
        else if (caption.startsWith("/addVegetable"))
            addNewItemToDatabase(update, ItemType.vegetable);
        else if (caption.startsWith("/addHerb"))
            addNewItemToDatabase(update, ItemType.herb);
        else if(caption.startsWith("?"))
            sendFileId(update);
        else
            sendAdminWarning(update);
    }

    private void processCallBackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        switch (callbackData) {
            case ButtonUtils.FRUITS_CALLBACK, ButtonUtils.HERB_CALLBACK,
                    ButtonUtils.BERRY_CALLBACK, ButtonUtils.VEGETABLES_CALLBACK -> getSubCatalog(update);
        }if(callbackData.startsWith("CALLBACK")){
            getPersonalItemPage(update);
        }else if(callbackData.startsWith(ButtonUtils.BACK_FROM_PERSONAL_PAGE)){
            backToSubCatalogAndRefreshPhoto(update);
        }else if(callbackData.startsWith(ButtonUtils.BACK_FROM_SUB_CATALOG)) {
            backToMainCatalogAndRefreshPic(update);
        }else if(callbackData.startsWith(ButtonUtils.ADD_TO_CART_CALLBACK)){
            addToCardRequestReceived(update);
        }else if(callbackData.startsWith(ButtonUtils.DELETE_ITEM_CALLBACK)){
            deleteItem(update);
        }else if(callbackData.startsWith(ButtonUtils.UNIT_KG_CALLBACK) || callbackData.startsWith(ButtonUtils.UNIT_PCS_CALLBACK)){
            unitInput(update);
        }else if (callbackData.startsWith(ButtonUtils.DELETE_FROM_CART_CALLBACK)){
            deleteFromCartRequestReceived(update);
        } else if (callbackData.startsWith(ButtonUtils.CLEAR_CART_CALLBACK)) {
            clearCartRequestReceived(update);
        } else if (callbackData.startsWith(ButtonUtils.CHANGE_PRICE_CALLBACK) && adminValidator.isOwner(update)){
            changePriceRequestReceived(update);
        } else if(callbackData.startsWith(ButtonUtils.CREATE_ORDER_CALLBACK)){
            startOrder(update);
        }
    }

    private void mailingRequestReceived(Update update) {
        if (adminValidator.isOwner(Extractor.extractChatIdFromUpdate(update))) {
            String mailingText = update.getMessage().getText().replace("/sendAll ", "");

            List<User> userList = null;

                userList = userService.findAll();

            int counter = doMassMailingAndCountIt(userList, mailingText);

            setMainKeyboardAndExecute(messageUtils.generateSendMessage(update, messageUtils.MAILING_WAS_ENDED + counter));
            return;
        }
        unsupportedMessageReceived(update);
    }

    private int doMassMailingAndCountIt(List<User> userList, String mailingText) {
        int counter = 0;

        SendMessage sendMessage = new SendMessage();

        if (mailingText.contains("{NAME}")) {
            for (User user : userList) {
                String personalMailingText = mailingText.replace("{NAME}", user.getFirstName());
                sendMessage.setText(personalMailingText);
                sendMessage.setChatId(user.getChatId());
                try {
                    this.telegramBot.execute(sendMessage);
                    counter++;
                }catch (TelegramApiException e) {
                    log.error("Error while sending message to user with chatId: " + sendMessage.getChatId() + " cause: " + e.getMessage());
                }
            }
        } else {
            sendMessage.setText(mailingText);
            for (User user : userList) {
                sendMessage.setChatId(user.getChatId());

                try {
                    this.telegramBot.execute(sendMessage);
                    counter++;
                }catch (TelegramApiException e) {
                    log.error("Error while sending message to user with chatId: " + sendMessage.getChatId() + " cause: " + e.getMessage());
                }

            }
        }

        return counter;
    }
    private void processMessageInOrderStatus(Update update) {
        if(userDataCache.getCurrentUserBotState(update).equals(BotState.WAIT_FOR_MOBILE_NUMBER))
            processMobileNumber(update);
        else if(userDataCache.getCurrentUserBotState(update).equals(BotState.WAIT_FOR_ADDRESS))
            processAddress(update);
    }

    private void startOrder(Update update) {
        deleteMessage(update);

        UserCartData userCartData = userDataCache.getUserCart().get(Extractor.extractChatIdFromUpdate(update));
        SendMessage sendMessage;

        if(userCartData == null || userCartData.getItemsInCart().isEmpty()){
            sendMessage = messageUtils.generateSendMessage(update, messageUtils.CART_IS_EMPTY);
        } else {
            sendMessage = messageUtils.generateSendMessage(update, messageUtils.INSERT_MOBILE_NUMBER);
            userDataCache.setCurrentBotState(update, BotState.WAIT_FOR_MOBILE_NUMBER);
        }

        setMainKeyboardAndExecute(sendMessage);
    }

    private void processMobileNumber(Update update) {
        UserCartData userCartData = userDataCache.getUserCart().get(Extractor.extractChatIdFromUpdate(update));
        SendMessage sendMessage;

        if(userCartData == null || userCartData.getItemsInCart().isEmpty()){
            sendMessage = messageUtils.generateSendMessage(update, messageUtils.CART_IS_EMPTY);
            userDataCache.setCurrentBotState(update, BotState.MAIN);
        }else {
            String messageText = update.getMessage().getText();
            if(messageText.matches("^\\+375(29|44|25)\\d{7}$")){
                userCartData.setNumber(messageText);
                sendMessage = messageUtils.generateSendMessage(update, messageUtils.NUMBER_ACCEPTED_WAIT_FOR_ADDRESS);
                userDataCache.setCurrentBotState(update, BotState.WAIT_FOR_ADDRESS);
            }else if(messageText.equalsIgnoreCase("cancel")){
                sendMessage = messageUtils.generateSendMessage(update, messageUtils.ORDER_CANCELLED);
                userDataCache.setCurrentBotState(update, BotState.MAIN);
            }else {
                sendMessage = messageUtils.generateSendMessage(update, messageUtils.WRONG_NUMBER_INPUT);
            }
        }

        setMainKeyboardAndExecute(sendMessage);
    }


    private void processAddress(Update update) {
        UserCartData userCartData = userDataCache.getUserCart().get(Extractor.extractChatIdFromUpdate(update));
        SendMessage sendMessage;

        if(userCartData == null || userCartData.getItemsInCart().isEmpty()){
            sendMessage = messageUtils.generateSendMessage(update, messageUtils.CART_IS_EMPTY);
            userDataCache.setCurrentBotState(update, BotState.MAIN);
        }else {
            String messageText = update.getMessage().getText();

            if(messageText.equalsIgnoreCase("cancel")){
                sendMessage = messageUtils.generateSendMessage(update, messageUtils.ORDER_CANCELLED);
                userDataCache.setCurrentBotState(update, BotState.MAIN);
            }else {
                sendMessage = messageUtils.generateSendMessage(update, messageUtils.ADDRESS_ACCEPTED);
                userCartData.setAddress(messageText);

                Long customerChatId = Extractor.extractChatIdFromUpdate(update);

                sendOrderToAdmins(userCartData);

                userDataCache.getUserCart().remove(customerChatId);
            }
        }

        setMainKeyboardAndExecute(sendMessage);
    }

    private void infoRequestReceived(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessage(update, messageUtils.INFO);
        setMainKeyboardAndExecute(sendMessage);
    }

    private void helpRequestReceived(Update update) {
        String messageText = update.getMessage().getText();
        SendMessage sendMessage;
        if(messageText.equals("/help"))
            sendMessage = messageUtils.generateSendMessage(update, messageUtils.HELP_MESSAGE);
        else
            sendMessage = messageUtils.generateSendMessage(update, messageUtils.DEVELOPER_HELP);

        setMainKeyboardAndExecute(sendMessage);
    }

    private void changePriceRequestReceived(Update update) {
        deleteMessage(update);
        Long itemId = Long.valueOf(update.getCallbackQuery().getData().replace(ButtonUtils.CHANGE_PRICE_CALLBACK, ""));

        userDataCache.setCurrentBotState(update, BotState.WAIT_FOR_NEW_PRICE);
        adminDataCache.addItemIdToAdminCache(update, itemId);
        setMainKeyboardAndExecute(messageUtils.generateSendMessage(update, messageUtils.INSERT_NEW_AMOUNT));
        log.info("Set bot state WAIT_FOR_NEW_PRICE for user " + update.getCallbackQuery().getFrom().getFirstName() + " with chatId: "+ Extractor.extractChatIdFromUpdate(update));
    }

    private void clearCartRequestReceived(Update update) {
        deleteMessage(update);
        SendMessage sendMessage;
        if (!userDataCache.getUserCart().containsKey(Extractor.extractChatIdFromUpdate(update))) {
            sendMessage = messageUtils.generateSendMessage(update, messageUtils.CART_IS_EMPTY);
        }else {
            userDataCache.clearCart(update);
            logUtils.writeDefaultLogInfoMessage(update, "sent a request to reset their cart");
            sendMessage = messageUtils.generateSendMessage(update, "Cart has been cleared");
        }

        setMainKeyboardAndExecute(sendMessage);
    }


    private void processOrdinalNumber(Update update, String messageText) {
        if(DoubleValidator.isRound(messageText)) {
            doubleDigitReceived(update);
        }else if(messageText.equalsIgnoreCase("cancel")) {
            cancelOrder(update);
        }else
            sendIncorrectDigitInputWarning(update);
    }


    private void processAmountMessage(Update update, String messageText) {
        if(userDataCache.getTransitionalItemCart().get(Extractor.extractChatIdFromUpdate(update))
                .getUnit().equals(Unit.pcs)){
            if(DoubleValidator.isRound(messageText)){
                doubleDigitReceived(update);
            }else if(messageText.equalsIgnoreCase("cancel")) {
                cancelOrder(update);
            }else {
                sendIncorrectDigitInputWarning(update);
            }
        } else if(DoubleValidator.isDouble(messageText)) {
            doubleDigitReceived(update);
        }else if(messageText.equalsIgnoreCase("cancel")) {
            cancelOrder(update);
        }else
            sendIncorrectDigitInputWarning(update);
    }

    private void processNewPrice(Update update) {
        String messageText = update.getMessage().getText();

        if(DoubleValidator.isDouble(messageText)){
            Double newPrice = DoubleValidator.parseDouble(messageText);

            try {
                Long itemId = adminDataCache.getItemIdByAdminChatId(update);
                itemService.changePrice(itemId, newPrice);
            }catch (AdminCacheNotContainSuchChatId | ItemNotFoundException e){
                log.error(logUtils.getDefaultMessageWithUserFNAndChatId(update) + " caused an error " + e.getMessage());
            }

            userDataCache.setCurrentBotState(update, BotState.MAIN);
            adminDataCache.removeAdminFromCache(update);
            SendMessage sendMessage = messageUtils.generateSendMessage(update, messageUtils.PRICE_SUCCESSFULLY_CHANGED);
            executeMessage(sendMessage);
        }else if(messageText.equalsIgnoreCase("cancel")) {
            cancelOrder(update);
        }else
            sendIncorrectNewPriceWarning(update);
    }


    private void deleteFromCartRequestReceived(Update update) {
        deleteMessage(update);

        SendMessage sendMessage = messageUtils.generateSendMessage(update, MessageUtils.DELETE_ITEM_FROM_CART_MESSAGE);
        userDataCache.setCurrentBotState(update, BotState.WAIT_FOR_ORDINAL_NUMBER_FOR_DELETING);

        log.info("Set bot state WAIT_FOR_ORDINAL_NUMBER_FOR_DELETING for user " + Extractor.extractChatIdFromUpdate(update));

        setMainKeyboardAndExecute(sendMessage);
    }

    private void sendCartToUser(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(Extractor.extractChatIdFromUpdate(update));

        try {
            String cartData = messageUtils.generateUserCart(update);
            sendMessage.setReplyMarkup(keyboardUtils.getCartKeyboard());
            sendMessage.setText(cartData);
            executeMessage(sendMessage);
            return;
        }catch (CartIsEmptyException e){
            sendMessage.setText(MessageUtils.CART_IS_EMPTY);

            log.error(e.getMessage());
        }

        setMainKeyboardAndExecute(sendMessage);
    }

    private void doubleDigitReceived(Update update) {
        if(userDataCache.getCurrentUserBotState(update).equals(BotState.WAIT_FOR_AMOUNT)){
            SendMessage sendMessage;
            try {
                userDataCache.addToUserCart(update);
                sendMessage = messageUtils.generateSendMessage(update, messageUtils.SUCCESSFULLY_ADDED_TO_CART);
            } catch (IncompatibleUnitsException e){
                logUtils.writeDefaultLogInfoMessage(update, " tried to add to cart same items with different units");
                userDataCache.resetTransactionalCart(update);
                userDataCache.setCurrentBotState(update, BotState.MAIN);
                sendMessage = messageUtils.generateSendMessage(update, messageUtils.INCOMPATIBLE_UNITS);
            } catch (CartOutOfBoundException e){
                logUtils.writeDefaultLogInfoMessage(update, " tried to add to cart more then 15 items" + e.getMessage());
                userDataCache.resetTransactionalCart(update);
                userDataCache.setCurrentBotState(update, BotState.MAIN);
                sendMessage = messageUtils.generateSendMessage(update, messageUtils.CART_LIMIT_WARNING);
            } catch (IllegalAmountException e){
                logUtils.writeDefaultLogInfoMessage(update, " tried to add to cart more then 15 kilo or 100pcs" + e.getMessage());
                userDataCache.resetTransactionalCart(update);
                userDataCache.setCurrentBotState(update, BotState.MAIN);
                sendMessage = messageUtils.generateSendMessage(update, messageUtils.ORDER_LIMIT_WARNING);
            }

            executeMessage(sendMessage);
        } else if (userDataCache.getCurrentUserBotState(update).equals(BotState.WAIT_FOR_ORDINAL_NUMBER_FOR_DELETING)) {
            SendMessage sendMessage;

            try {
                userDataCache.deleteFromUserCartByOrdinalNumber(update);
                sendMessage = messageUtils.generateSendMessage(update, MessageUtils.SUCCESSFULLY_DELETED_FROM_CART);
                logUtils.writeDefaultLogInfoMessage(update, "successfully deleted item " + update.getMessage().getText() + " from cart");
            } catch (CartNotContainsSuchOrdinalNumber e){
                logUtils.writeDefaultLogInfoMessage(update, e.getMessage());
                sendMessage = messageUtils.generateSendMessage(update, MessageUtils.CART_DOES_NOT_CONTAIN_SUCH_A_ORDINAL_NUMBER);
            } catch (CartIsEmptyException e){
                logUtils.writeDefaultLogInfoMessage(update, e.getMessage());
                sendMessage = messageUtils.generateSendMessage(update, messageUtils.CART_IS_EMPTY);
            }

            setMainKeyboardAndExecute(sendMessage);
        } else {
            unsupportedMessageReceived(update);
            userDataCache.setCurrentBotState(update, BotState.MAIN);
        }
    }

    private void unitInput(Update update) {
        if(!userDataCache.getCurrentUserBotState(update).equals(BotState.WAIT_FOR_UNIT)){
            deleteMessage(update);
            SendMessage sendMessage = messageUtils.generateSendMessage(update, messageUtils.END_EVERY_ORDER_WARNING);
            executeMessage(sendMessage);
            sendCatalogToUser(update);
            logUtils.writeDefaultLogInfoMessage(update, " got \"finish order\" warning");
            return;
        }

        userDataCache.addUnitToTransitItem(update);
        EditMessageText editMessageText = new EditMessageText();

        if(userDataCache.getTransitionalItemCart().get(Extractor.extractChatIdFromUpdate(update)).getUnit().equals(Unit.kg))
            editMessageText.setText(messageUtils.ENTER_KG_AMOUNT);
        else
            editMessageText.setText(messageUtils.ENTER_PCS_AMOUNT);

        editMessageText.setChatId(Extractor.extractChatIdFromUpdate(update));
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        executeMessage(editMessageText);


        userDataCache.setCurrentBotState(update, BotState.WAIT_FOR_AMOUNT);

        log.info("Set bot state WAIT_FOR_AMOUNT for user " + Extractor.extractChatIdFromUpdate(update));
    }

    private void addToCardRequestReceived(Update update) {
        Long itemId = Long.valueOf(update.getCallbackQuery().getData().replace(ButtonUtils.ADD_TO_CART_CALLBACK, ""));
        String title;
        if(userDataCache.getCurrentUserBotState(update).equals(BotState.MAIN)){
            try{
                title = itemService.findById(itemId).getTitle();
                userDataCache.addToTransitionalCart(Extractor.extractChatIdFromUpdate(update), itemId);
                log.info("Item " + title + " were added to transactional cart of the user " + update.getCallbackQuery().getFrom().getFirstName() +
                        " with chatId " + Extractor.extractChatIdFromUpdate(update));
            }catch (ItemNotFoundException e) {
                SendMessage sendMessage = messageUtils.generateSendMessage(update, messageUtils.ITEM_NOT_FOUND_WARNING);
                setMainKeyboardAndExecute(sendMessage);
                log.error("User " + update.getCallbackQuery().getFrom().getUserName() + " with chatId" + Extractor.extractChatIdFromUpdate(update) + " send request to get non-existed item\n" + e.getMessage());
                return;
            }

            deleteMessage(update);

            sendChooseUnitMessageToUser(update, itemId);
        }else {
            resetBotStateAndTransactionalCart(update);
            try{
                title = itemService.findById(itemId).getTitle();
                userDataCache.addToTransitionalCart(Extractor.extractChatIdFromUpdate(update), itemId);
                log.info("item " + title + " were added to transactional cart of the user " + update.getCallbackQuery().getFrom().getFirstName() +
                        " with chatId " + Extractor.extractChatIdFromUpdate(update));
            }catch (ItemNotFoundException e) {
                SendMessage sendMessage = messageUtils.generateSendMessage(update, messageUtils.ITEM_NOT_FOUND_WARNING);
                setMainKeyboardAndExecute(sendMessage);
                log.error("User " + update.getCallbackQuery().getFrom().getUserName() + " with chatId" + update.getCallbackQuery().getMessage().getChatId() + " send request to get non-existed item\n" + e.getMessage());
                return;
            }
            deleteMessage(update);

            sendChooseUnitMessageToUser(update, itemId);
        }
    }

    private void sendChooseUnitMessageToUser(Update update, Long itemId) {
        SendMessage sendMessage = messageUtils.chooseUnit(update, itemId);
        executeMessage(sendMessage);
        userDataCache.setCurrentBotState(update, BotState.WAIT_FOR_UNIT);
        log.info("Set bot state " + BotState.WAIT_FOR_UNIT + " for user " + Extractor.extractChatIdFromUpdate(update));
    }

    private void deleteItem(Update update) {
        if(!adminValidator.isOwner(update)){
            unsupportedMessageReceived(update);
            return;
        }

        itemService.deleteItem(update);
        SendMessage sendMessage = messageUtils.generateSendMessage(update, messageUtils.SUCCESSFULLY_DELETED);

        setMainKeyboardAndExecute(sendMessage);

        deleteMessage(update);
    }

    private void addNewItemToDatabase(Update update, ItemType itemType) {
        try {
            SendMessage sendMessage = itemService.saveItemToDatabase(update, itemType);
            executeMessage(sendMessage);
        }catch (ItemNotSaveException e){
            log.info(e.getMessage());
            sendAdminWarning(update);
        }
    }

    private void sendCatalogToUser(Update update) {
        if(userDataCache.getCurrentUserBotState(update) == BotState.MAIN) {
            SendPhoto sendPhoto = messageUtils.generateMainCatalog(update);
            executeMessage(sendPhoto);
            logUtils.writeDefaultLogInfoMessage(update, "get catalog");
        }else {
            resetBotStateAndTransactionalCart(update);

            SendPhoto sendPhoto = messageUtils.generateMainCatalog(update);
            executeMessage(sendPhoto);
            logUtils.writeDefaultLogInfoMessage(update, "get catalog");
        }
    }

    private void cancelOrder(Update update) {
        SendMessage sendMessage;
        if(userDataCache.getCurrentUserBotState(update).equals(BotState.MAIN)){
            sendMessage = messageUtils.generateSendMessage(update, messageUtils.NOTHING_TO_CANCEL);
            setMainKeyboardAndExecute(sendMessage);
            return;
        } else if (adminDataCache.isCacheContain(update)) {
            sendMessage = messageUtils.generateSendMessage(update, messageUtils.CANCEL_ADMIN);
            adminDataCache.removeAdminFromCache(update);
            executeMessage(sendMessage);
            return;
        } else {
            sendMessage = messageUtils.generateSendMessage(update, messageUtils.CANCEL_ORDER);
        }
        setMainKeyboardAndExecute(sendMessage);

        resetBotStateAndTransactionalCart(update);
    }

    private void resetBotStateAndTransactionalCart(Update update){
        Long chatId = Extractor.extractChatIdFromUpdate(update);
        userDataCache.setCurrentBotState(chatId, BotState.MAIN);
        userDataCache.resetTransactionalCart(update);
    }

    private void registerUser(Update update) {
        if(userService.findByChatId(Extractor.extractChatIdFromUpdate(update)).isEmpty()) {
            userService.saveUser(update);
            log.info("New user: " + Extractor.extractFirstnameFromUpdate(update) + " with chatId: " + Extractor.extractChatIdFromUpdate(update) + " send /start command and was save to database");
        }


        SendMessage sendMessage = messageUtils.generateSendMessage(update, messageUtils.WELCOME_MESSAGE);
        setMainKeyboardAndExecute(sendMessage);
    }

    private void getPersonalItemPage(Update update) {
        try {
            itemService.findById(update.getCallbackQuery().getData());
        }catch (ItemNotFoundException e){
            deleteMessage(update);
            SendMessage sendMessage = messageUtils.generateSendMessage(update, messageUtils.ITEM_NOT_FOUND_WARNING);
            setMainKeyboardAndExecute(sendMessage);
            log.error("User " + update.getCallbackQuery().getFrom().getUserName() + " with chatId" + Extractor.extractChatIdFromUpdate(update) + " send request to get non-existed item\n" + e.getMessage());
            return;
        }

        EditMessageMedia editMessageMedia = messageUtils.getPersonalItemPicture(update);
        executeMessage(editMessageMedia);

        EditMessageCaption editMessageCaption = messageUtils.getPersonalItemCaption(update);
        executeMessage(editMessageCaption);
    }

    private void getSubCatalog(Update update) {
        EditMessageMedia editMessageMedia = messageUtils.getSubCatalogPicture(update);
        executeMessage(editMessageMedia);
        EditMessageCaption editMessageCaption = messageUtils.getSubCatalog(update);
        executeMessage(editMessageCaption);
        logUtils.writeDefaultLogInfoMessage(update, "get fruits catalog");
    }

    private void backToSubCatalogAndRefreshPhoto(Update update) {
        EditMessageMedia editMessageMedia = messageUtils.getSubCatalogPicture(update);
        executeMessage(editMessageMedia);
        EditMessageCaption editMessageCaption = messageUtils.getSubCatalog(update);
        executeMessage(editMessageCaption);
        logUtils.writeDefaultLogInfoMessage(update, "get fruits catalog");
    }

    private void backToMainCatalogAndRefreshPic(Update update) {
        if(!userDataCache.getCurrentUserBotState(update).equals(BotState.MAIN))
            resetBotStateAndTransactionalCart(update);

        EditMessageMedia editMessageMedia = messageUtils.getMainCatalogPicture(update);

        executeMessage(editMessageMedia);

        EditMessageCaption editMessageCaption = messageUtils.getMainCatalogCapture(update);

        executeMessage(editMessageCaption);
    }

    private void setMainKeyboardAndExecute(SendMessage sendMessage) {
        sendMessage.setReplyMarkup(keyboardUtils.getMainKeyboard());
        executeMessage(sendMessage);
    }

    private void executeMessage(SendMessage sendMessage) {
        try {
            telegramBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error while sending message to user with chatId: " + sendMessage.getChatId() + " cause: " + e.getMessage());
        }
    }

    private void executeMessage(EditMessageCaption editMessageCaption) {
        try {
            telegramBot.execute(editMessageCaption);
        } catch (TelegramApiException e) {
            log.error("Error while sending message to user with chatId: " + editMessageCaption.getChatId() + " cause: " + e.getMessage());
        }
    }

    private void executeMessage(EditMessageText editMessageText) {
        try {
            telegramBot.execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error("Error while sending message to user with chatId: " + editMessageText.getChatId() + " cause: " + e.getMessage());
        }
    }

    private void executeMessage(DeleteMessage deleteMessage) {
        try {
            telegramBot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error("Error while deleting message to user with chatId: " + deleteMessage.getChatId() + " cause: " + e.getMessage());
        }
    }

    private void executeMessage(SendPhoto sendPhoto) {
        try {
            telegramBot.execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.error("Error while sending message to user with chatId: " + sendPhoto.getChatId() + " cause: " + e.getMessage());
        }
    }

    private void executeMessage(EditMessageMedia editMessageMedia) {
        try {
            telegramBot.execute(editMessageMedia);
        } catch (TelegramApiException e) {
            log.error("Error while sending message to user with chatId: " + editMessageMedia.getChatId() + " cause: " + e.getMessage());
        }
    }

    private void deleteMessage(Update update){
        DeleteMessage deleteMessage = messageUtils.generateDeleteMessage(update);
        executeMessage(deleteMessage);
    }

    private void sendFileId(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessage(update, update.getMessage().getPhoto().get(update.getMessage().getPhoto().size() - 1).getFileId());
        executeMessage(sendMessage);
    }

    private void sendAdminWarning(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessage(update, messageUtils.ADMIN_WARNING);
        executeMessage(sendMessage);
    }

    private void unsupportedMessageReceived(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessage(update, messageUtils.UNSUPPORTED_COMMAND);
        executeMessage(sendMessage);

        logUtils.writeDefaultLogInfoMessage(update, " got unsupported message received warning");
    }

    private void sendIncorrectDigitInputWarning(Update update) {
        SendMessage sendMessage;
        if(userDataCache.getCurrentUserBotState(update).equals(BotState.WAIT_FOR_ORDINAL_NUMBER_FOR_DELETING))
            sendMessage = messageUtils.generateSendMessage(update, messageUtils.INCORRECT_ORDINAL_NUMBER_INPUT_WARNING);
        else if(userDataCache.getTransitionalItemCart().get(Extractor.extractChatIdFromUpdate(update)).getUnit().equals(Unit.kg))
            sendMessage = messageUtils.generateSendMessage(update, messageUtils.INCORRECT_DIGIT_INPUT_WARNING);
        else
            sendMessage = messageUtils.generateSendMessage(update, messageUtils.INCORRECT_ROUND_DIGIT_INPUT_WARNING);
        setMainKeyboardAndExecute(sendMessage);
    }

    private void sendIncorrectNewPriceWarning(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessage(update, messageUtils.INCORRECT_PRICE_INPUT_WARNING);
        setMainKeyboardAndExecute(sendMessage);
    }

    private void sendFinishOrderWarning(Update update) {
        if (update.hasCallbackQuery())
        deleteMessage(update);

        SendMessage sendMessage = messageUtils.generateSendMessage(update, messageUtils.FINISH_ORDER_WARNING);

        executeMessage(sendMessage);

        if(userDataCache.getCurrentUserBotState(update).equals(BotState.WAIT_FOR_MOBILE_NUMBER))
            sendMessage = messageUtils.generateSendMessage(update, messageUtils.INSERT_MOBILE_NUMBER);
        else if(userDataCache.getCurrentUserBotState(update).equals(BotState.WAIT_FOR_ADDRESS))
            sendMessage = messageUtils.generateSendMessage(update, messageUtils.NUMBER_ACCEPTED_WAIT_FOR_ADDRESS);

        executeMessage(sendMessage);
    }

    private void sendOrderToAdmins(UserCartData userCartData) {
        List<Long> adminIds = adminValidator.getOwners();

        String dataCart = messageUtils.generateUserCart(userCartData);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(dataCart);
        for (Long chatId : adminIds) {
            sendMessage.setChatId(chatId);
            executeMessage(sendMessage);
        }
    }
}

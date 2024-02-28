package pl.shakhner.PetrushkaProductsBot.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import pl.shakhner.PetrushkaProductsBot.botAPI.BotState;
import pl.shakhner.PetrushkaProductsBot.botAPI.TransitionalItem;
import pl.shakhner.PetrushkaProductsBot.botAPI.Unit;
import pl.shakhner.PetrushkaProductsBot.botAPI.UserCartData;
import pl.shakhner.PetrushkaProductsBot.exceptions.CartIsEmptyException;
import pl.shakhner.PetrushkaProductsBot.exceptions.CartNotContainsSuchOrdinalNumber;
import pl.shakhner.PetrushkaProductsBot.exceptions.CartOutOfBoundException;
import pl.shakhner.PetrushkaProductsBot.exceptions.IllegalAmountException;
import pl.shakhner.PetrushkaProductsBot.services.ItemService;
import pl.shakhner.PetrushkaProductsBot.services.servicesImpl.ItemServiceImpl;
import pl.shakhner.PetrushkaProductsBot.util.*;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class UserDataCache {
    private Map<Long, BotState> usersBotState = new HashMap<>();
    private Map<Long, UserCartData> userCart = new HashMap<>();
    private Map<Long, TransitionalItem> transitionalItemCart = new HashMap<>();
    private final LogUtils logUtils;
    private final ItemService itemService;
    private final Extractor chatIdExtractor;

    @Autowired
    public UserDataCache(LogUtils logUtils, ItemService itemService, Extractor chatIdExtractor) {
        this.logUtils = logUtils;
        this.itemService = itemService;
        this.chatIdExtractor = chatIdExtractor;
    }

    public BotState getCurrentUserBotState(Update update){

        long chatId;

        chatId = update.hasCallbackQuery() ? update.getCallbackQuery().getMessage().getChatId() : update.getMessage().getChatId();
        BotState botState = usersBotState.get(chatId);

        if(botState == null)
            botState = botState.MAIN;

        return botState;
    }

    public void setCurrentBotState(Update update, BotState botState){
        long chatId;
        if(update.hasCallbackQuery())
            chatId = update.getCallbackQuery().getMessage().getChatId();
        else
            chatId = update.getMessage().getChatId();
        usersBotState.put(chatId, botState);
    }

    public void setCurrentBotState(Long chatId, BotState botState){
        usersBotState.put(chatId, botState);
    }

    public Map<Long, TransitionalItem> getTransitionalItemCart() {
        return transitionalItemCart;
    }

    public void addToTransitionalCart(Long chatId, Long itemId){
        TransitionalItem transitionalItem = new TransitionalItem();
        itemService.findById(itemId);

        transitionalItem.setItemId(itemId);
        transitionalItemCart.put(chatId, transitionalItem);
    }

    public void resetTransactionalCart(Update update) {
        if(update.hasCallbackQuery())
            transitionalItemCart.remove(update.getCallbackQuery().getMessage().getChatId());
        else
            transitionalItemCart.remove(update.getMessage().getChatId());

        logUtils.writeDefaultLogInfoMessage(update, " got new resetted and empty transactional cart");
    }

    public void resetAllTransactionalCarts(){
        transitionalItemCart = new HashMap<>();

        log.info("All transactional carts were resetted");
    }

    public void resetAllCarts() {
        userCart = new HashMap<>();

        log.info("All carts were resetted");
    }

    public void resetAllBotStates() {
        usersBotState = new HashMap<>();

        log.info("All users bot states were resetted");
    }



    public void addToUserCart(Update update) {
        emptyCartCheck(update);
        long chatId = chatIdExtractor.extractChatIdFromUpdate(update);

        TransitionalItem transitionalItem = transitionalItemCart.get(chatId);
        String expectedFloat = update.getMessage().getText().replace(",", ".");
        Float amount = Float.parseFloat(expectedFloat);

        if(transitionalItem.getUnit().equals(Unit.kg)) {
            if (amount > 15 || amount <= 0)
                throw new IllegalAmountException(Extractor.extractChatIdFromUpdate(update), Extractor.extractFirstnameFromUpdate(update));
        }else if (transitionalItem.getUnit().equals(Unit.pcs)){
            if(amount > 100 || amount <= 0)
                throw new IllegalAmountException(Extractor.extractChatIdFromUpdate(update), Extractor.extractFirstnameFromUpdate(update));
        }else if(userCart.get(chatId).getItemsInCart().size() > 20)
            throw new CartOutOfBoundException(Extractor.extractChatIdFromUpdate(update), Extractor.extractFirstnameFromUpdate(update));

        transitionalItem.setAmount(amount);

        userCart.get(chatId).addToCart(transitionalItem, update);

        setCurrentBotState(update, BotState.MAIN);
    }

    private void emptyCartCheck(Update update) {
        UserCartData userCartData = userCart.get(chatIdExtractor.extractChatIdFromUpdate(update));
        if(userCartData == null) {
            generateEmptyCart(update);
            logUtils.writeDefaultLogInfoMessage(update, " made a request to add to an empty cart, so an empty cart was generated for him");
        }
    }

    private void generateEmptyCart(Update update){
        userCart.put(chatIdExtractor.extractChatIdFromUpdate(update), new UserCartData());
    }

    public Map<Long, UserCartData> getUserCart() {
        return userCart;
    }

    public void addUnitToTransitItem(Update update) {
        if(update.getCallbackQuery().getData().startsWith("k")){
            transitionalItemCart.get(chatIdExtractor.extractChatIdFromUpdate(update)).setUnit(Unit.kg);
        }else {
            transitionalItemCart.get(chatIdExtractor.extractChatIdFromUpdate(update)).setUnit(Unit.pcs);
        }
    }

    public void deleteFromUserCartByOrdinalNumber(Update update) {
        long chatId = chatIdExtractor.extractChatIdFromUpdate(update);
        UserCartData userCartData = userCart.get(chatId);

        if (userCartData == null) {
            throw new CartIsEmptyException(ExceptionUtils.cartIsEmptyExceptionBuilder(update));
        }

        Map<Long, Integer> itemIdOrdinalIdMap = userCartData.getOrdinalItemNumberItemId();
        String ordinalNumberText = update.getMessage().getText();

        if (itemIdOrdinalIdMap.containsValue(Integer.valueOf(ordinalNumberText))) {
            long itemIdToRemove = -1;

            for (Map.Entry<Long, Integer> entry : itemIdOrdinalIdMap.entrySet()) {
                if (entry.getValue() == Integer.valueOf(ordinalNumberText)) {
                    itemIdToRemove = entry.getKey();
                    break;
                }
            }

            userCartData.getItemsInCart().remove(itemIdToRemove);
            itemIdOrdinalIdMap.values().removeIf(value -> value == Integer.valueOf(ordinalNumberText));

            int currentOrdinalNumber = 1;

            for (Long itemId : itemIdOrdinalIdMap.keySet()) {
                itemIdOrdinalIdMap.put(itemId, currentOrdinalNumber++);
            }

            setCurrentBotState(update, BotState.MAIN);
        } else {
            throw new CartNotContainsSuchOrdinalNumber(logUtils.getDefaultMessageWithUserFNAndChatId(update) + " tried to delete a non-existent ordinal number");
        }
    }

    public void clearCart(Update update) {
        userCart.put(Extractor.extractChatIdFromUpdate(update), new UserCartData());
        logUtils.writeDefaultLogInfoMessage(update, " got new cart");
    }

    public boolean isUserInOrderStatus(Update update) {
        Long chatId = Extractor.extractChatIdFromUpdate(update);
        return (userCart.containsKey(chatId) &&
                (usersBotState.get(chatId).equals(BotState.WAIT_FOR_MOBILE_NUMBER) || usersBotState.get(chatId).equals(BotState.WAIT_FOR_ADDRESS)));
    }
}

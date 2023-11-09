package pl.shakhner.PetrushkaProductsBot.botAPI;

import org.telegram.telegrambots.meta.api.objects.Update;
import pl.shakhner.PetrushkaProductsBot.exceptions.IllegalAmountException;
import pl.shakhner.PetrushkaProductsBot.exceptions.IncompatibleUnitsException;
import pl.shakhner.PetrushkaProductsBot.util.Extractor;

import java.util.HashMap;
import java.util.Map;

public class UserCartData {
    private Map<Long, ItemInCart> itemsInCart = new HashMap<>();
    private Map<Long, Integer> ordinalItemNumberItemId = new HashMap<>();
    private String address;
    private String number;

    public void addToCart(TransitionalItem transitionalItem, Update update){

        if(isItemAlreadyExistInCart(transitionalItem)){
            incrementAmountOfExistedItem(transitionalItem, update);
            return;
        }

        ItemInCart itemInCart = new ItemInCart(transitionalItem.getUnit(), transitionalItem.getAmount());
        itemsInCart.put(transitionalItem.getItemId(), itemInCart);
        ordinalItemNumberItemId.put(transitionalItem.getItemId(), ordinalItemNumberItemId.size() + 1);
    }

    private boolean isItemAlreadyExistInCart(TransitionalItem item) {
        return itemsInCart.containsKey(item.getItemId());
    }

    public void incrementAmountOfExistedItem(TransitionalItem transitionalItem, Update update){
        Long itemId = transitionalItem.getItemId();

        if(!itemsInCart.get(itemId).getUnit().equals(transitionalItem.getUnit()))
            throw new IncompatibleUnitsException("Единица измерения существующего в корзине товара другая.\nУдалите товар из корзины и добавьте заново");
        else if(itemsInCart.get(itemId).getUnit().equals(Unit.pcs)){
            if(itemsInCart.get(itemId).getAmount() + transitionalItem.getAmount() > 100)
                throw new IllegalAmountException(Extractor.extractChatIdFromUpdate(update), Extractor.extractFirstnameFromUpdate(update));
        }else if(itemsInCart.get(itemId).getUnit().equals(Unit.kg)){
            if(itemsInCart.get(itemId).getAmount() + transitionalItem.getAmount() > 15)
                throw new IllegalAmountException(Extractor.extractChatIdFromUpdate(update), Extractor.extractFirstnameFromUpdate(update));
        }
        itemsInCart.get(itemId).setAmount(itemsInCart.get(itemId).getAmount() + transitionalItem.getAmount());
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Map<Long, ItemInCart> getItemsInCart() {
        return itemsInCart;
    }

    public void setItemsInCart(Map<Long, ItemInCart> itemsInCart) {
        this.itemsInCart = itemsInCart;
    }

    public Map<Long, Integer> getOrdinalItemNumberItemId() {
        return ordinalItemNumberItemId;
    }

    public void setOrdinalItemNumberItemId(Map<Long, Integer> ordinalItemNumberItemId) {
        this.ordinalItemNumberItemId = ordinalItemNumberItemId;
    }
}

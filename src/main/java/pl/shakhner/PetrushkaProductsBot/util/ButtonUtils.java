package pl.shakhner.PetrushkaProductsBot.util;

import org.springframework.stereotype.Component;
import pl.shakhner.PetrushkaProductsBot.botAPI.Unit;

@Component
public class ButtonUtils {
    public static final String FRUITS_BUTTON = "Fruits";
    public static final String FRUITS_CALLBACK = "fruit";
    public static final String VEGETABLES_BUTTON = "Vegetables";
    public static final String VEGETABLES_CALLBACK = "vegetable";
    public static final String HERB_BUTTON = "Herbs";
    public static final String HERB_CALLBACK = "herb";
    public static final String BERRY_BUTTON = "Berries";
    public static final String BERRY_CALLBACK = "berry";
    public static final String BACK_FROM_PERSONAL_PAGE = "Back_P_";
    public static final String BACK_FROM_SUB_CATALOG = "Back_S";
    public static final String DELETE_ITEM_CALLBACK = "DELETE_ITEM_";
    public static final String DELETE_ITEM_FROM_CART_TEXT = "Delete item from cart ";
    public static final String CLEAR_CART_TEXT = "Clean cart";
    public static final String DELETE_FROM_CART_CALLBACK = "DELETE_FROM_CART_CALLBACK";
    public static final String CLEAR_CART_CALLBACK = "CLEAR_CART_CALLBACK";
    public static final String CREATE_ORDER_TEXT = "Make an order";
    public static final String CREATE_ORDER_CALLBACK = "CREATE_ORDER_CALLBACK";
    public static final String CHANGE_PRICE_CALLBACK = "CHANGE_PRICE_CALLBACK";
    public static final String PCS_TEXT = "Pcs";
    public static final String KG_TEXT = "Kg";
    public static final String CHANGE_PRICE_TEXT = "Change the price";
    public static final String DELETE_ITEM_TEXT = "Delete product";
    public static final String BACK_TEXT = "Back";
    public static String ADD_TO_CART_BUTTON = "Add to cart";
    public static String ADD_TO_CART_CALLBACK = "Add_";
    public static String UNIT_KG_CALLBACK = Unit.kg.toString();
    public static String UNIT_PCS_CALLBACK = Unit.pcs.toString();
}

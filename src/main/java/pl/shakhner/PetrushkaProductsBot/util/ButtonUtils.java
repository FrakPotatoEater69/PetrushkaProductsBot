package pl.shakhner.PetrushkaProductsBot.util;

import org.springframework.stereotype.Component;
import pl.shakhner.PetrushkaProductsBot.botAPI.Unit;

@Component
public class ButtonUtils {
    public static final String FRUITS_BUTTON = "Фрукты";
    public static final String FRUITS_CALLBACK = "fruit";
    public static final String VEGETABLES_BUTTON = "Овощи";
    public static final String VEGETABLES_CALLBACK = "vegetable";
    public static final String HERB_BUTTON = "Травы";
    public static final String HERB_CALLBACK = "herb";
    public static final String BERRY_BUTTON = "Ягоды";
    public static final String BERRY_CALLBACK = "berry";
    public static final String BACK_FROM_PERSONAL_PAGE = "Back_P_";
    public static final String BACK_FROM_SUB_CATALOG = "Back_S";
    public static final String DELETE_ITEM_CALLBACK = "DELETE_ITEM_";
    public static final String DELETE_ITEM_FROM_CART_TEXT = "Удалить предмет из корзины";
    public static final String CLEAR_CART_TEXT = "Очистить корзину";
    public static final String DELETE_FROM_CART_CALLBACK = "DELETE_FROM_CART_CALLBACK";
    public static final String CLEAR_CART_CALLBACK = "CLEAR_CART_CALLBACK";
    public static final String CREATE_ORDER_TEXT = "Оформить заказ";
    public static final String CREATE_ORDER_CALLBACK = "CREATE_ORDER_CALLBACK";
    public static final String CHANGE_PRICE_CALLBACK = "CHANGE_PRICE_CALLBACK";
    public static final String PCS_TEXT = "Шт";
    public static final String KG_TEXT = "Кг";
    public static final String CHANGE_PRICE_TEXT = "Изменить цену";
    public static final String DELETE_ITEM_TEXT = "Удалить продукт";
    public static final String BACK_TEXT = "Назад";
    public static String ADD_TO_CART_BUTTON = "Добавить в корзину";
    public static String ADD_TO_CART_CALLBACK = "Add_";
    public static String UNIT_KG_CALLBACK = Unit.kg.toString();
    public static String UNIT_PCS_CALLBACK = Unit.pcs.toString();
}

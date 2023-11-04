package pl.shakhner.PetrushkaProductsBot.botAPI;

public enum BotState {
    WAIT_FOR_UNIT,
    WAIT_FOR_AMOUNT,
    WAIT_FOR_ADDRESS,
    MAIN,
    WAIT_FOR_MOBILE_NUMBER,
    WAIT_FOR_ORDINAL_NUMBER_FOR_DELETING,
    WAIT_FOR_NEW_PRICE
}

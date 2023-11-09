package pl.shakhner.PetrushkaProductsBot.util;

import org.telegram.telegrambots.meta.api.objects.Update;

public class ExceptionUtils {
    public static String cartIsEmptyExceptionBuilder(Update update){
        return "User " + update.getMessage().getFrom().getFirstName() + " with chatId " + Extractor.extractChatIdFromUpdate(update) + " send request to an empty cart ";
    }
}

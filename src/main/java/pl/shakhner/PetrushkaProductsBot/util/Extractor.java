package pl.shakhner.PetrushkaProductsBot.util;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class Extractor {

    public static Long extractChatIdFromUpdate(Update update){
        if(update.hasCallbackQuery())
            return update.getCallbackQuery().getMessage().getChatId();
        else if(update.hasEditedMessage())
            return update.getEditedMessage().getChatId();

        return update.getMessage().getChatId();
    }

    public static String extractFirstnameFromUpdate(Update update){
        if(update.hasCallbackQuery())
            return update.getCallbackQuery().getMessage().getFrom().getFirstName();
        else if(update.hasEditedMessage())
            return update.getEditedMessage().getFrom().getFirstName();

        return update.getMessage().getFrom().getFirstName();
    }

}

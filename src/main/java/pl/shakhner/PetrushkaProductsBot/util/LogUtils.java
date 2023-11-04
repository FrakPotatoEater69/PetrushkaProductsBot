package pl.shakhner.PetrushkaProductsBot.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class LogUtils {
    private final Extractor chatIdExtractor;

    public LogUtils(Extractor chatIdExtractor) {
        this.chatIdExtractor = chatIdExtractor;
    }

    public void writeDefaultLogInfoMessage(Update update, String message){

        log.info(getDefaultMessageWithUserFNAndChatId(update) +
                message);
    }

    public String getDefaultMessageWithUserFNAndChatId(Update update){
        String firstName = null;
        long chatId = chatIdExtractor.extractChatIdFromUpdate(update);

        if (update.hasMessage()) {
            firstName = update.getMessage().getFrom().getFirstName();
        }
        else {
            firstName = update.getCallbackQuery().getFrom().getFirstName();
        }
        return "User " + firstName + " with chatId " + chatId;
    }
}

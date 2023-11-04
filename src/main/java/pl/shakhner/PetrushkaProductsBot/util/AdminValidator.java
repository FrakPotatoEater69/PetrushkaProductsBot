package pl.shakhner.PetrushkaProductsBot.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Component
public class AdminValidator {
    @Value("${owners}")
    private String stringOwners;

    private List<Long> owners = new ArrayList<>();

    @PostConstruct
    private void convertIdFromStringToLong() {
        if (stringOwners.length() > 9) {
            String[] stringIds = stringOwners.split(", ");
            for (String id : stringIds) {
                owners.add(Long.parseLong(id));
            }
        } else {
            owners.add(Long.parseLong(stringOwners));
        }
    }

    public boolean isOwner(Long id) {
        return owners.contains(id);
    }

    public boolean isOwner(Update update){
        long chatId;

        if(update.hasCallbackQuery())
            chatId = update.getCallbackQuery().getMessage().getChatId();
        else
            chatId = update.getMessage().getChatId();

        return owners.contains(chatId);
    }

    public List<Long> getOwners() {
        return owners;
    }
}

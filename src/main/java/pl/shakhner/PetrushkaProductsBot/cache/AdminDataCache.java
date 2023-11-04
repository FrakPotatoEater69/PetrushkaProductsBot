package pl.shakhner.PetrushkaProductsBot.cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import pl.shakhner.PetrushkaProductsBot.exceptions.AdminCacheNotContainSuchChatId;
import pl.shakhner.PetrushkaProductsBot.util.Extractor;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class AdminDataCache {

    private Map<Long, Long> adminIdNewPriceCache = new HashMap<>();

    public void addItemIdToAdminCache(Update update, Long itemId){

        adminIdNewPriceCache.put(Extractor.extractChatIdFromUpdate(update), itemId);
        log.info(update.getCallbackQuery().getFrom().getFirstName() + " was added to admin cache" + " with itemId: " + itemId);
    }

    public void removeAdminFromCache(Update update){
        Long chatId = Extractor.extractChatIdFromUpdate(update);
        if(adminIdNewPriceCache.containsKey(chatId)){
            adminIdNewPriceCache.remove(chatId);
            log.info(chatId + " was removed from admin cache");
        }else
            throw new AdminCacheNotContainSuchChatId("Cant remove " + chatId + " from Admin cache because its not contain it");
    }

    public Long getItemIdByAdminChatId(Update update){
        if(!isCacheContain(update))
            return adminIdNewPriceCache.get(Extractor.extractChatIdFromUpdate(update));
        throw new AdminCacheNotContainSuchChatId("Cant get itemId from Admin cache because its not contain provided chatId: " + Extractor.extractChatIdFromUpdate(update));
    }

    public Boolean isCacheContain(Update update){
        return adminIdNewPriceCache.containsValue(Extractor.extractChatIdFromUpdate(update));
    }
}

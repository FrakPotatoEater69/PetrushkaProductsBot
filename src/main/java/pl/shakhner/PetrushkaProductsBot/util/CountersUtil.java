package pl.shakhner.PetrushkaProductsBot.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.shakhner.PetrushkaProductsBot.cache.UserDataCache;

@Component
@Slf4j
public class CountersUtil {

    private final UserDataCache userDataCache;

    public CountersUtil(UserDataCache userDataCache) {
        this.userDataCache = userDataCache;
    }

    @Scheduled(cron = "0 31 16 * * ?")
    public void updateCartsAtMidnight() {
        log.info("Midnight reboot starts");
        userDataCache.resetAllTransactionalCarts();
        userDataCache.resetAllCarts();
        userDataCache.resetAllBotStates();
        log.info("Midnight reboot finished");
        }
    }



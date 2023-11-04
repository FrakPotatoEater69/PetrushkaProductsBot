package pl.shakhner.PetrushkaProductsBot.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimiter {
    private static final Map<String, Long> REQUESTS = new ConcurrentHashMap<>();

    private static final int REQUESTS_PER_SECOND = 1;
    private static final long TIME_UNIT_IN_MILLISECONDS = TimeUnit.SECONDS.toMillis(1);

    public static boolean isRequestAllowed(String chatId) {
        Long lastRequestTime = REQUESTS.get(chatId);
        long currentTime = System.currentTimeMillis();

        if (lastRequestTime == null || currentTime - lastRequestTime > TIME_UNIT_IN_MILLISECONDS) {
            REQUESTS.put(chatId, currentTime);
            return true;
        }

        int requestsInCurrentTimeUnit = REQUESTS.entrySet().stream()
                .filter(entry -> entry.getKey().equals(chatId) && currentTime - entry.getValue() <= TIME_UNIT_IN_MILLISECONDS)
                .mapToInt(entry -> 1)
                .sum();

        return requestsInCurrentTimeUnit < REQUESTS_PER_SECOND;
    }


}

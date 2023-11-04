package pl.shakhner.PetrushkaProductsBot.exceptions;

public class AdminCacheNotContainSuchChatId extends RuntimeException {
    public AdminCacheNotContainSuchChatId (String cause){
        super(cause);
    }
}

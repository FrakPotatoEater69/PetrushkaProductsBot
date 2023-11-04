package pl.shakhner.PetrushkaProductsBot.exceptions;

import org.telegram.telegrambots.meta.api.objects.Update;

public class IllegalAmountException extends RuntimeException{
    public IllegalAmountException(Long chatId, String name){
        super("User: " + name + " with chatId " + chatId + " tried to add item with amount more then 15 kilo or mire then 10 pcs");
    }
}

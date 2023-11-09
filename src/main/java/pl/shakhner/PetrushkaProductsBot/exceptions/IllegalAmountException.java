package pl.shakhner.PetrushkaProductsBot.exceptions;

public class IllegalAmountException extends RuntimeException{
    public IllegalAmountException(Long chatId, String name){
        super("User: " + name + " with chatId " + chatId + " tried to add item with amount more then 15 kilo or mire then 10 pcs");
    }
}

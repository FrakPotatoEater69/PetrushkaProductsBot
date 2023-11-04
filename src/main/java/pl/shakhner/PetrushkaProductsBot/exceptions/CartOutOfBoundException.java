package pl.shakhner.PetrushkaProductsBot.exceptions;

public class CartOutOfBoundException extends RuntimeException{
    public CartOutOfBoundException(Long chatId, String name){
        super("User: " + name + " with chatId " + chatId + " tried to add in cart more then 20 items");
    }
}

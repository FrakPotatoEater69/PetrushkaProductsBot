package pl.shakhner.PetrushkaProductsBot.exceptions;

public class ItemNotFoundException extends RuntimeException{
    public ItemNotFoundException (String cause){
        super(cause);
    }
}

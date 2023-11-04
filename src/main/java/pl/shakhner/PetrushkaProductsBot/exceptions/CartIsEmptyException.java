package pl.shakhner.PetrushkaProductsBot.exceptions;

public class CartIsEmptyException extends RuntimeException{
    public CartIsEmptyException (String cause){
        super(cause);
    }
}

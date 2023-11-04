package pl.shakhner.PetrushkaProductsBot.exceptions;

public class ItemNotSaveException extends RuntimeException {
    public ItemNotSaveException(String cause){
        super(cause);
    }
}

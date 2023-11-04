package pl.shakhner.PetrushkaProductsBot.exceptions;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String cause){
        super(cause);
    }
}

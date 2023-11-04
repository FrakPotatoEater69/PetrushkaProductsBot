package pl.shakhner.PetrushkaProductsBot.exceptions;

public class CartNotContainsSuchOrdinalNumber extends RuntimeException{
    public CartNotContainsSuchOrdinalNumber(String cause){
        super(cause);
    }
}

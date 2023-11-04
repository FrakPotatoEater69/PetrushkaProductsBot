package pl.shakhner.PetrushkaProductsBot.exceptions;

public class IncompatibleUnitsException extends RuntimeException {
    public IncompatibleUnitsException(String cause){
        super(cause);
    }
}

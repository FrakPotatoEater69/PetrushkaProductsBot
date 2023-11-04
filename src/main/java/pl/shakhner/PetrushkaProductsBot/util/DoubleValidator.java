package pl.shakhner.PetrushkaProductsBot.util;

public class DoubleValidator {
    public static boolean isDouble(String expectedDouble) {
        return expectedDouble.matches("\\d+((\\.|,)\\d+)?");
    }

    public static Double parseDouble(String expectedDouble){
        expectedDouble = expectedDouble.replace(',', '.');

        return Double.parseDouble(expectedDouble);
    }

    public static Boolean isRound(String expectedRoundDouble){

        return (isDouble(expectedRoundDouble) && parseDouble(expectedRoundDouble) % 1 == 0);
    }
}

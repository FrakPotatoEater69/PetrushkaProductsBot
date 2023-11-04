package pl.shakhner.PetrushkaProductsBot.botAPI;

public class ItemInCart {
    private Unit unit;
    private Float amount;

    public ItemInCart(Unit unit, Float amount) {
        this.unit = unit;
        this.amount = amount;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }
}

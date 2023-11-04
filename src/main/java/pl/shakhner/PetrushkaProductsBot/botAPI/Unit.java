package pl.shakhner.PetrushkaProductsBot.botAPI;

public enum Unit {
    kg("Кг"),
    pcs("Штук");

    private String title;
    Unit(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

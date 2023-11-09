package pl.shakhner.PetrushkaProductsBot.botAPI;

public enum Unit {
    kg("Kg"),
    pcs("Pcs");

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

package pl.shakhner.PetrushkaProductsBot.dto;


public class ItemPriceTitleDTO {

    private Long id;

    private Double price;

    private String title;

    public ItemPriceTitleDTO(Long id, Double price, String title) {
        this.id = id;
        this.price = price;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

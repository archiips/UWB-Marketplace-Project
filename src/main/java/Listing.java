import java.util.Date;
import java.time.LocalDateTime;

public class Listing {

    private double price;
    private String name;
    private String description;
    private boolean inStock;
    private final LocalDateTime createdAt;

    public Listing(double price, String name, String description) {
        this.price = price;
        this.name = name;
        this.description = description;
        this.inStock = true;
        this.createdAt = LocalDateTime.now();
    }

    public Listing() {
        this.inStock = true;
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Name: " + name + "\nPrice" + price +
                "\nDescription: " + description + "\nStock status: " + inStock +
                "\nDate added: " + createdAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Listing other = (Listing) obj;

        if (Double.compare(other.price, price) != 0) {
            return false;
        }
        if (inStock != other.inStock) {
            return false;
        }
        if (name != null ? !name.equals(other.name) : other.name != null) {
            return false;
        }
        return description != null ? description.equals(other.description) : other.description == null;
    }

    public void delete() {
        name = null;
        price = 0.0;
        description = null;
        inStock = false;
    }

    // Getters

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isInStock() {
        return inStock;
    }

    //Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStock(boolean inStock) {
        this.inStock = inStock;
    }

}

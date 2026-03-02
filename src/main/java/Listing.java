import java.util.Date;

public class Listing {

    private double price;
    private String name;
    private String description;
    private boolean sold;
    private Date dateAdded;

    public Listing(String name, String description, double price, Date dateAdded, boolean sold) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.name = name;
        this.description = description;
        this.price = price;
        this.dateAdded = dateAdded;
        this.sold = sold;
    }

    public Listing(double price, String name, String description) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price;
        this.name = name;
        this.description = description;
        this.sold = false;
        this.dateAdded = new Date();
    }

    public Listing() {
        this.sold = false;
        this.dateAdded = new Date();
    }

    @Override
    public String toString() {
        return "Name: " + name + "\nPrice" + price +
                "\nDescription: " + description + "\nStock status: " + sold +
                "\nDate added: " + dateAdded;
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
        if (sold != other.sold) {
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
        sold = true;
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

    public Date getDateAdded() {
        return dateAdded;
    }

    public boolean isSold() {
        return sold;
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

    public void setSold(boolean sold) {
        this.sold = sold;
    }

}

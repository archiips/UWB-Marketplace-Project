import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Store {
    private static final String FILE_NAME = "listings.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final Type LIST_TYPE = new TypeToken<List<Listing>>() {}.getType();

    // Reads all listings in listings.json
    private List<Listing> readAllListings() throws IOException {
        File file = new File(FILE_NAME);

        // create file if it doesn't exist
        if (!file.exists()) {
            FileWriter writer = new FileWriter(file);
            writer.write("[]");
            writer.close();
        }

        FileReader reader = new FileReader(file);

        List<Listing> listings = gson.fromJson(reader, LIST_TYPE);
        reader.close();
        if (listings == null){
            listings = new ArrayList<>();
        }

        return listings;
    }

    // Lists stored listings
    public List<Listing> listStoredListings() throws IOException {
        List<Listing> listings = readAllListings();
        if (listings.isEmpty()) {
            System.out.println("(No Listings Found)");
            return listings;
        }

        int i = 1;
        for (Listing l : listings) {
            System.out.println(i++ + ") " + l.getName() + " | $" + l.getPrice() + " | " + (l.isSold() ? "SOLD" : "AVAILABLE"));
        }

        return listings;
    }

    // Writes all listings in List listings to listings.json
    private void writeAllListings(List<Listing> listings) throws IOException {
        FileWriter writer = new FileWriter(FILE_NAME);

        gson.toJson(listings, writer);

        writer.close();
    }

    public void deleteListing(int index) throws IOException {
        List<Listing> listings = readAllListings();
        if (index < 0 || index >= listings.size()) {
            throw new IndexOutOfBoundsException("Invalid listing index: " + index);
        }
        listings.remove(index);
        writeAllListings(listings);
    }

    public void storeListing(Listing listing) throws IOException {
        if (listing == null) {
            throw new IllegalArgumentException("listing cannot be null");
        }

        List<Listing> listings = readAllListings();
        if (!listings.contains(listing)) {   // uses Listing.equals(...)
            listings.add(listing);
            writeAllListings(listings);
        }
    }

}

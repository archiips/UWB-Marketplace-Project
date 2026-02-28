import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StoreTests {

    @TempDir
    File file;

    @Test
    void testLoadFromJson() throws Exception {
        File jsonFile = new File(file,"jsonFile.txt");

        String json = """
        [
          {
            "name": "Chair",
            "description": "Good",
            "price": 40.0,
            "dateAdded": 0,
            "sold": false
          }
        ]
        """;

        /*
        Exact implementation tbd
         */
        Files.writeString(jsonFile.toPath(), json);

        //TODO: Will be compilable with creation of Store object
        Store storage = new Store(jsonFile);
        List<Listing> listings = storage.listStoredListings();

        assertNotNull(listings);
        assertEquals(1, listings.size());
        assertEquals(1, listings.size());
        assertEquals("Chair", listings.get(0).getName());
        assertEquals("Good", listings.get(0).getDescription());
        assertEquals(40.0, listings.get(0).getPrice(), 0.0001);
        assertFalse(listings.get(0).isSold());
        assertNotNull(listings.get(0).getDateAdded());
    }

    @Test
    void testSaveToJson(){
        // TODO: Implement based off agreed implementation of Store object
    }
}

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;


public class ListingTests {

    @Test
    void testListingCreation(){
        //Example input for listing creation
        String name = "Desk Lamp";
        String description = "Works great";
        double price = 15.00;
        Date date = new Date(0);
        boolean sold = false;

        Listing listing = new Listing(name, description, price, date, sold);

        assertNotNull(listing);
        assertEquals(name, listing.getName());
        assertEquals(description, listing.getDescription());
        assertEquals(price, listing.getPrice(), 0.0001);
        assertEquals(date, listing.getDateAdded());
        assertEquals(sold, listing.isSold());
    }

    @Test
    void testGettersSetters() {
        Listing l = new Listing();
        l.setName("Chair");
        l.setDescription("Good condition");
        l.setPrice(40.0);
        l.setSold(true);

        assertEquals("Chair", l.getName());
        assertEquals("Good condition", l.getDescription());
        assertEquals(40.0, l.getPrice(), 0.0001);
        assertTrue(l.isSold());
    }

    // Should Return 'True' as listings are equal
    @Test
    void testEqualsSameFields() {
        Date d = new Date(0);
        Listing a = new Listing("Lamp", "Works", 15.0, d, false);
        Listing b = new Listing("Lamp", "Works", 15.0, d, false);

        assertEquals(a, b);
    }

    // Should Return 'False' as listings are not equal
    @Test
    void testEqualsDiffFields() {
        Date d = new Date(0);
        Listing a = new Listing("Lamp", "Works", 15.0, d, false);
        Listing b = new Listing("Lamp", "Works", 16.0, d, false);

        assertNotEquals(a, b);
    }

    @Test
    void testInvalidPrice() {
        Executable ex = () ->
                new Listing("Lamp", "Damaged", -10.0, new Date(0), false);

        assertThrows(IllegalArgumentException.class, ex);
    }

}

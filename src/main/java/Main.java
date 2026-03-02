import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Store store = new Store();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("================================");
        System.out.println("   UW Campus Marketplace");
        System.out.println("================================");

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                listListingsMenu();
            } else if (choice.equals("2")) {
                createListingMenu();
            } else if (choice.equals("3")) {
                deleteListingMenu();
            } else if (choice.equals("4")) {
                System.out.println("\nGoodbye!");
                running = false;
            } else {
                System.out.println("\nInvalid option. Please enter 1-4.\n");
            }
        }

        scanner.close();
    }

    private static void printMainMenu() {
        System.out.println("--- Main Menu ---");
        System.out.println("1. List Listings");
        System.out.println("2. Create Listing");
        System.out.println("3. Delete Listing");
        System.out.println("4. Exit");
        System.out.print("Choose an option: ");
    }

    private static void listListingsMenu() {
        System.out.println("\n--- Listings ---");
        List<Listing> listings;

        try {
            listings = store.listStoredListings();
        } catch (IOException e) {
            System.out.println("Error reading listings: " + e.getMessage());
            return;
        }

        if (listings.isEmpty()) {
            System.out.println("Press Enter to go back...");
            scanner.nextLine();
            return;
        }

        System.out.println("Enter a listing number to view details, or 0 to go back:");
        System.out.print("Choice: ");
        String input = scanner.nextLine().trim();

        int choice;
        try {
            choice = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("\nInvalid input. Going back.\n");
            return;
        }

        if (choice == 0) {
            System.out.println();
            return;
        }

        if (choice < 1 || choice > listings.size()) {
            System.out.println("\nInvalid listing number. Going back.\n");
            return;
        }

        Listing selected = listings.get(choice - 1);
        System.out.println("\n--- Listing Details ---");
        System.out.println(selected.toString());
        System.out.println("\nPress Enter to go back...");
        scanner.nextLine();
        System.out.println();
    }

    private static void deleteListingMenu() {
        System.out.println("\n--- Delete Listing ---");
        List<Listing> listings;

        try {
            listings = store.listStoredListings();
        } catch (IOException e) {
            System.out.println("Error reading listings: " + e.getMessage());
            return;
        }

        if (listings.isEmpty()) {
            System.out.println("Press Enter to go back...");
            scanner.nextLine();
            return;
        }

        System.out.println("Enter a listing number to delete, or 0 to go back:");
        System.out.print("Choice: ");
        String input = scanner.nextLine().trim();

        int choice;
        try {
            choice = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("\nInvalid input. Going back.\n");
            return;
        }

        if (choice == 0) {
            System.out.println();
            return;
        }

        if (choice < 1 || choice > listings.size()) {
            System.out.println("\nInvalid listing number. Going back.\n");
            return;
        }

        try {
            store.deleteListing(choice - 1);
            System.out.println("\nListing deleted successfully!\n");
        } catch (IOException e) {
            System.out.println("Error deleting listing: " + e.getMessage());
        }
    }

    private static void createListingMenu() {
        System.out.println("\n--- Create Listing ---");

        System.out.print("Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Description: ");
        String description = scanner.nextLine().trim();

        double price = -1;
        while (price < 0) {
            System.out.print("Price ($): ");
            String priceInput = scanner.nextLine().trim();
            try {
                price = Double.parseDouble(priceInput);
                if (price < 0) {
                    System.out.println("Price cannot be negative. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid price. Enter a number (e.g. 9.99).");
            }
        }

        Listing listing = new Listing(price, name, description);

        try {
            store.storeListing(listing);
            System.out.println("\nListing created successfully!");
        } catch (IOException e) {
            System.out.println("Error saving listing: " + e.getMessage());
        }

        System.out.println("Press Enter to go back...");
        scanner.nextLine();
        System.out.println();
    }
}

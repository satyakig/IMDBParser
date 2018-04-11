import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Random;
import java.util.ArrayList;


public class Item {
    public String name;
    public Double price;

    public static String[] names = {
        "Pop",
        "Popcorn",
        "Burger",
        "Nachos",
        "Coffee",
        "Tea",
        "Fries",
        "Poutine",
        "Burrito",
        "Hot Dog"
    };

    public static Double[] prices = { 1.50, 2.00, 3.50, 4.20, 5.30, 5.75, 6.90, 7.88, 8.00, 9.99 };

    public Item(String n, Double p) {
        name = n;
        price = p;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public static Item[] getRandomItems() {
        int totalItems = 10;
        Item[] items = new Item[totalItems];

        ArrayList<String> itemNames = new ArrayList<>();
        for(int i = 0; i < totalItems; i++) {
            items[i] = new Item(names[i], prices[getRandom(0, 9)]);
        }

        return items;
    }

    public static int getRandom(int lo, int hi) {
        Random rand = new Random();
        int random = rand.nextInt(hi + 1 - lo) + lo;

        return random;
    }

    public static void main(String[] args) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Item[] items = getRandomItems();

        for(int i = 0; i < items.length; i++)
            System.out.print(items[i].name + ", ");
        System.out.println();

        String jsonString = gson.toJson(items);
        System.out.println(jsonString);

    }


}

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
        int totalItems = getRandom(8, 0);
        totalItems = totalItems + 2;
        Item[] items = new Item[totalItems];

        items[0] = new Item(names[0], prices[getRandom(9, 0)]);
        items[1] = new Item(names[1], prices[getRandom(9, 0)]);

        ArrayList<String> itemNames = new ArrayList<>();
        for(int i = 2; i < totalItems; i++) {
            String tmp = names[getRandom(9, 2)];

            while(itemNames.contains(tmp))
                tmp = names[getRandom(9, 2)];

            items[i] = new Item(tmp, prices[getRandom(9, 0)]);
        }

        return items;
    }

    public static int getRandom(int hi, int lo) {
        Random rand = new Random();
        int random = rand.nextInt(hi - lo) + lo;

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

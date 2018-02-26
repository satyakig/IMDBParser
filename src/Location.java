import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;

public class Location {

    public String locationID;
    public String name;
    public String address;
    public Item[] items;
    public Theatre[] theatres;

    public static int total = 6;
    public static String[] ids = {
            "Eau Claire",
            "Chinook",
            "Sunridge",
            "Westhills",
            "Crowfoot",
            "CrossIron Mills"
    };

    public static String[] names = {
            "Cineplex Odeon Eau Claire Market Cinemas",
            "Scotiabank Theatre Chinook",
            "Cineplex Odeon Sunridge Spectrum Cinemas",
            "Cineplex Odeon Westhills Cinemas",
            "Cineplex Odeon Crowfoot Crossing Cinemas",
            "SilverCity CrossIron Mills Cinemas and XSCAPE Entertainment Centre"
    };

    public static String[] adds = {
            "Eau Claire Market, 200 Barclay Parade SW, 200 Barclay Parade S.W., Calgary, AB, T2P 4R5",
            "6455 Macleod Trail SW , Calgary, AB, T2H 0K4",
            "#400, 2555-32nd Street NE, Calgary, AB, T1Y 7J6",
            "165 Stewart Green SW, Calgary, AB, T3H 3C8",
            "91 Crowfoot Terrace NW, Calgary, AB, T3G 2L5",
            "261055 CrossIron Boulevard, Rocky View, AB, T4A 0G3"
    };

    public Location(String l, String n, String a, Item[] i, Theatre[] t) {
        locationID = l;
        name = n;
        address = a;
        items = i;
        theatres = t;
    }

    public static Location[] getLocations() {
        Location[] locations = new Location[6];

        for(int x = 0; x < total; x++) {
            String l = ids[x];
            String n = names[x];
            String a = adds[x];

            Item[] items = Item.getRandomItems();
            Theatre[] theatres = Theatre.getRandomTheatres();

            locations[x] = new Location(l, n, a, items, theatres);
        }

        return locations;
    }


    public static void main(String[] args) {
        Location[] locations = new Location[6];

        for(int x = 0; x < total; x++) {
            String l = ids[x];
            String n = names[x];
            String a = adds[x];

            Item[] items = Item.getRandomItems();
            Theatre[] theatres = Theatre.getRandomTheatres();

            locations[x] = new Location(l, n, a, items, theatres);
        }

        JsonObject obj = new JsonObject();
        Gson converter = new GsonBuilder().create();

        for(int i = 0; i < total; i++)
            obj.addProperty(ids[i], converter.toJson(locations[i]));

        try (FileWriter writer = new FileWriter("output.json")) {

            Gson gson = new GsonBuilder().create();
            gson.toJson(obj, writer);

        }catch(IOException err) {
            System.out.println(err.getMessage());
        }

    }
}


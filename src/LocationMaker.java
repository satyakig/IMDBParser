import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;


public class LocationMaker {
    public String key;
    public Location value;

    public LocationMaker(String k, Location v) {
        key = k;
        value = v;
    }

    public static void main(String[] args) {

        LocationMaker[] obj = new LocationMaker[Location.total];
        Location[] locations = Location.getLocations();


        for(int i = 0; i < Location.total; i++) {
            obj[i] = new LocationMaker(locations[i].locationID, locations[i]);
        }

        try (FileWriter writer = new FileWriter("locations.json")) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(obj, writer);

        }catch(IOException err) {
            System.out.println(err.getMessage());
        }
    }
}

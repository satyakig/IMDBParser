import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Random;

public class Theatre {
    public int theatreNum;
    public int totalRows;
    public int totalCols;

    public Theatre(int t, int r, int c) {
        theatreNum = t;
        totalRows = r;
        totalCols = c;
    }

    public static Theatre[] getRandomTheatres() {
        int total = getRandom(11, 6);
        Theatre[] theatres = new Theatre[total];

        for(int i = 0; i < total; i++) {
            int r = getRandom(15, 1);
            int c = getRandom(15, 5);

            theatres[i] = new Theatre(i, r, c);
        }

        return theatres;
    }

    public static int getRandom(int hi, int lo) {
        Random rand = new Random();
        int random = rand.nextInt(hi - lo) + lo;

        return random;
    }

    public static void main(String[] args) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Theatre[] theatres = getRandomTheatres();

        String jsonString = gson.toJson(theatres);
        System.out.println(jsonString);

    }
}

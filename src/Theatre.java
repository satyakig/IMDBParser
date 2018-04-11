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
        int total = getRandom(10, 15);
        Theatre[] theatres = new Theatre[total];

        for(int i = 0; i < total; i++) {
            int r = getRandom(5, 7);
            int c = getRandom(5, 7);

            theatres[i] = new Theatre(i, r, c);
        }

        return theatres;
    }

    public static int getRandom(int lo, int hi) {
        Random rand = new Random();
        int random = rand.nextInt(hi + 1 - lo) + lo;

        return random;
    }

    public static void main(String[] args) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Theatre[] theatres = getRandomTheatres();

        String jsonString = gson.toJson(theatres);
        System.out.println(jsonString);

    }
}

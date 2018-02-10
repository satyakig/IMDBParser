import java.util.*;

public class Exit implements Runnable {

    public void run() {
        Scanner inp = new Scanner(System.in);

        String line = "hello";

        while(Imdb_Omdb.run) {
            line = inp.nextLine();

            if(line.equalsIgnoreCase("q") || line.equalsIgnoreCase("quit"))
                Imdb_Omdb.run = false;
        }
    }
}

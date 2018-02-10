import java.util.*;

public class Exit implements Runnable {

    public boolean imdb;
    public Exit(boolean im) {
        imdb = im;
    }

    public void run() {
        Scanner inp = new Scanner(System.in);

        String line = "hello";

        while(true) {
            line = inp.nextLine();

            if(line.equalsIgnoreCase("q") || line.equalsIgnoreCase("quit")) {
                if(imdb)
                    Imdb_Omdb.run = false;
                else
                    Videos.run = false;
                break;
            }
        }
    }
}

import java.util.*;

public class Exit implements Runnable {

    Imdb_Omdb imdb = null;
    Videos videos = null;

    public Exit(Imdb_Omdb instance) {
        imdb = instance;
    }

    public Exit(Videos instance) {
        videos = instance;
    }

    public void run() {
        Scanner inp = new Scanner(System.in);

        String line = "hello";

        while(true) {
            line = inp.nextLine();

            if(line.equalsIgnoreCase("q") || line.equalsIgnoreCase("quit")) {
                if(imdb != null)
                    imdb.run = false;
                else
                    videos.run = false;
                break;
            }
        }
    }
}

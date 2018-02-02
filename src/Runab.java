import java.util.*;

public class Runab implements Runnable {

    public void run() {
        Scanner inp = new Scanner(System.in);

        String line = "hello";

        while(Imdb.run) {
            line = inp.nextLine();

            if(line.equalsIgnoreCase("q") || line.equalsIgnoreCase("quit"))
                Imdb.run = false;

            try {
                Thread.sleep(300000);
            }catch(InterruptedException err) {
                System.out.println(err.getMessage());
            }
        }
    }
}

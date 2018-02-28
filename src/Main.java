public class Main {

    public static void main(String[] args) {
        Imdb_Omdb.makeIDS();

        Exit one = new Exit(true);
        Thread t1 = new Thread(one);
        t1.setDaemon(true);
        t1.start();

        Imdb_Omdb.doPosts();




        Videos.readIDs();

        Exit two = new Exit(false);
        Thread t2 = new Thread(two);
        t2.setDaemon(true);
        t2.start();

        Videos.makeJson();
    }
}

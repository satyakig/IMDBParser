public class Main {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        Imdb_Omdb.makeIDS();
        Exit one = new Exit(true);
        Thread t1 = new Thread(one);
        t1.setDaemon(true);
        t1.start();
        Imdb_Omdb.removeOldFailed();
        Imdb_Omdb.doPosts();

        Videos.readIDs();
        Exit two = new Exit(false);
        Thread t2 = new Thread(two);
        t2.setDaemon(true);
        t2.start();
        Videos.makeJson();

        long end = System.currentTimeMillis();
        long diff = end - start;
        long hrs = (diff / 1000) / 3600;
        long mins = ((diff - (hrs * 3600 * 1000)) / 1000) / 60;
        System.out.println("\nTime elapsed = " + hrs +  "hrs  " + mins +  "mins  " + ((diff / 1000) % 60) + " secs");
    }
}

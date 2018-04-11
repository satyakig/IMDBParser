public class Main {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        Imdb_Omdb imdb = new Imdb_Omdb();
        imdb.start();

        Videos videos = new Videos();
        videos.start();

        long end = System.currentTimeMillis();
        long diff = end - start;
        long hrs = (diff / 1000) / 3600;
        long mins = ((diff - (hrs * 3600 * 1000)) / 1000) / 60;
        System.out.println("\nTime elapsed = " + hrs +  "hrs  " + mins +  "mins  " + ((diff / 1000) % 60) + " secs");
    }
}

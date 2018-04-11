import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.net.*;


public class Imdb_Omdb {
    public volatile boolean run;

    private ArrayList<String> allTitles;
    private ArrayList<String> checkFail;

    private ArrayList<String> currentMovies;
    private ArrayList<String> upcomingMovies;

    private JSONParser parser;
    private JSONObject current;
    private JSONObject upcoming;

    private long nowUnix;
    private long lastYearUnix;

    private SimpleDateFormat releasedFormat;

    public Imdb_Omdb() {
        run = true;

        allTitles = new ArrayList<>();
        checkFail = new ArrayList<>();

        currentMovies = new ArrayList<>();
        upcomingMovies = new ArrayList<>();

        parser = new JSONParser();
        current = new JSONObject();
        upcoming = new JSONObject();

        releasedFormat = new SimpleDateFormat("dd MMM yyyy");
        nowUnix = new Date().getTime();

        try {
            lastYearUnix = new SimpleDateFormat("yyyy/MM/dd").parse("2017/01/01").getTime();
        }catch (Exception err) {
            System.out.println(err.getMessage());
            System.exit(1);
        }
    }

    public void start() {
        long start = System.currentTimeMillis();

        Exit one = new Exit(this);
        Thread t1 = new Thread(one);
        t1.setDaemon(true);
        t1.start();

        this.makeIDS();
        this.removeOldFailed();
        this.doPosts();

        long end = System.currentTimeMillis();
        long diff = end - start;
        long hrs = (diff / 1000) / 3600;
        long mins = ((diff - (hrs * 3600 * 1000)) / 1000) / 60;
        System.out.println("\nTime elapsed for IMDB = " + hrs +  "hrs  " + mins +  "mins  " + ((diff / 1000) % 60) + " secs");
        System.out.println("Total = " + allTitles.size() + ", Failed = " + checkFail.size() + ", Passed = " + (currentMovies.size() + upcomingMovies.size()) +
                ", Current = " + currentMovies.size() + ", Upcoming = " + upcomingMovies.size());
    }

    private void makeIDS() {
        TsvParserSettings settings = new TsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        TsvParser parser = new TsvParser(settings);

        List<String[]> imdb;

        try {
            imdb = parser.parseAll(new FileReader("data.tsv"));
            System.out.println("Done reading data.tsv - " + imdb.size() + " entries \n");

            for(int i = imdb.size() - 1; i > 0; i--) {
                String id = imdb.get(i)[0];
                String type = imdb.get(i)[1];
                String startYear = imdb.get(i)[5];

                if(!startYear.equalsIgnoreCase("/N") && type.equalsIgnoreCase("movie")) {
                    try {
                        int year = Integer.parseInt(startYear);

                        if(year >= 2017)
                            allTitles.add(id);
                    }catch(NumberFormatException e) {   }
                }
            }

            PrintWriter printer = new PrintWriter(new File("allMovies.txt"));
            for(int i = 1; i < allTitles.size(); i++)
                printer.println(allTitles.get(i));
            printer.close();

            System.out.println("\nDone making titles - " + allTitles.size() + " entries\n");

        }catch(FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private void removeOldFailed() {
        try {
            System.out.println("Removing old failed titles...");
            Thread.sleep(2500);
        }catch(InterruptedException e) { }

        try {
            Scanner scanner1 = new Scanner(new File("files/failedMovies.txt"));

            while (scanner1.hasNextLine())
                checkFail.add(scanner1.nextLine());

            scanner1.close();
            System.out.println("All Movies - " + allTitles.size() + ", Failed Movies - " + checkFail.size());

            for(int i = 0; i < allTitles.size(); i++) {
                String title = allTitles.get(i);
                for(int j = 0; j < checkFail.size(); j++) {
                    if(title.equalsIgnoreCase(checkFail.get(j))) {
                        allTitles.remove(i);
                        break;
                    }
                }
            }
        }catch(FileNotFoundException err) {
            System.out.println("File Not Found: " + err.getMessage());
        }
    }

    private void doPosts() {
        System.out.println("\nStarting doPosts... " + allTitles.size() + " entries\n");

        ArrayList<String> currentMovies = new ArrayList<>();
        ArrayList<String> upcomingMovies = new ArrayList<>();

        int total = 0, failed = 0, pass = 0, cur = 0, upc = 0;

        for(int index = allTitles.size() - 1; index >=0 && run; index--) {
            String id = allTitles.get(index);

            if(total % 200 == 0)
                System.out.println("Done = " + total + ", Left = " + (allTitles.size() - total) + ", Passed = " + pass + ", Failed = " + failed + ", Current = " + cur + ", Upcoming = " + upc);

            try {
                String res = get(id);
                Object tmpObj = parser.parse(res);
                JSONObject tempJson = (JSONObject) tmpObj;

                boolean tit = !tempJson.get("Title").toString().equalsIgnoreCase("N/A");
                boolean rate = !tempJson.get("Rated").toString().equalsIgnoreCase("N/A");
                boolean rel = !tempJson.get("Released").toString().equalsIgnoreCase("N/A");
                boolean run = !tempJson.get("Runtime").toString().equalsIgnoreCase("N/A");
                boolean gen = !tempJson.get("Genre").toString().equalsIgnoreCase("N/A");
                boolean dir = !tempJson.get("Director").toString().equalsIgnoreCase("N/A");
                boolean act = !tempJson.get("Actors").toString().equalsIgnoreCase("N/A");
                boolean plot = !tempJson.get("Plot").toString().equalsIgnoreCase("N/A");
                boolean lang = tempJson.get("Language").toString().contains("English");
                boolean coun = tempJson.get("Country").toString().contains("USA");
                boolean post = !tempJson.get("Poster").toString().equalsIgnoreCase("N/A");
                boolean rati = !tempJson.get("imdbRating").toString().equalsIgnoreCase("N/A");
                boolean box = !tempJson.get("BoxOffice").toString().equalsIgnoreCase("N/A");


                if(tempJson.get("Response").toString().equalsIgnoreCase("True") && tit)
                {
                    if(rel && gen && dir && act && plot && lang && coun && post)
                    {
                        String released = tempJson.get("Released").toString();
                        try {
                            long unixRel = releasedFormat.parse(released).getTime();

                            if(unixRel < lastYearUnix){
                                checkFail.add(id);
                                failed++;
                            }
                            else if(unixRel <= nowUnix) {
                                if(rate && run && rati && box) {
                                    current.put(id, tmpObj);
                                    currentMovies.add(id);
                                    pass++;
                                    cur++;
                                }
                                else {
                                    checkFail.add(id);
                                    failed++;
                                }
                            }
                            else {
                                upcoming.put(id, tmpObj);
                                upcomingMovies.add(id);
                                pass++;
                                upc++;
                            }
                        }catch(Exception ParseException) {
                            checkFail.add(id);
                            failed++;
                        }
                    }
                    else {
                        checkFail.add(id);
                        failed++;
                    }
                }
                else {
                    checkFail.add(id);
                    failed++;
                }

            }catch(ParseException e) {
                failed++;
            }catch(Exception e) {
                failed++;
            }
            total++;
        }
        run = false;

        try {
            FileWriter p1 = new FileWriter("current.json");
            FileWriter p2 = new FileWriter("upcoming.json");

            PrintWriter p3 = new PrintWriter(new File("currentMovies.txt"));
            PrintWriter p4 = new PrintWriter(new File("upcomingMovies.txt"));
            PrintWriter p5 = new PrintWriter(new File("failedMovies.txt"));

            p1.write(current.toJSONString());
            p2.write(upcoming.toJSONString());

            for(int i = 0; i < currentMovies.size(); i++)
                p3.println(currentMovies.get(i));

            for(int i = 0; i < upcomingMovies.size(); i++)
                p4.println(upcomingMovies.get(i));

            for(int i = 0; i < checkFail.size(); i++)
                p5.println(checkFail.get(i));

            p1.close();
            p2.close();
            p3.close();
            p4.close();
            p5.close();

        }catch(IOException e) {
            e.printStackTrace();
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private String get(String id) throws Exception {
        String url = "http://www.omdbapi.com/?i=" + id + "&apikey=8aebf5c5";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("charset", "utf-8");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    public static void main(String[] args) {
        Imdb_Omdb imdb = new Imdb_Omdb();
        imdb.start();
    }
}
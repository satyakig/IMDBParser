import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;
import java.net.*;


public class Imdb_Omdb {
    public static volatile boolean run = true;

    public static void makeIDS() {
        TsvParserSettings settings = new TsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        TsvParser parser = new TsvParser(settings);

        List<String[]> allTitles;
        ArrayList<String> titles = new ArrayList<>();

        try {
            allTitles = parser.parseAll(new FileReader("data.tsv"));

            System.out.println("Done reading data.tsv - " + allTitles.size() + " entries \n");
            for(int i = allTitles.size() - 1; i > 0; i--) {
                String id = allTitles.get(i)[0];
                String type = allTitles.get(i)[1];
                String startYear = allTitles.get(i)[5];

                try {
                    int year = Integer.parseInt(startYear);
                    if(type.equals("movie") && (year == 2017 || year == 2018)) {
                        titles.add(id);
                        System.out.println(id + " passed, year - " + type + " " + year);
                    }
                }catch(NumberFormatException e) {
                    System.out.println(id + " number fail, " + type + " " + startYear);
                }
            }

            PrintWriter printer = new PrintWriter(new File("movies.txt"));
            for(int i = 1; i < titles.size(); i++)
                printer.println(titles.get(i));
            printer.close();

            System.out.println("\nDone making titles - " + titles.size() + " entries\n");

        }catch(FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void doPosts() {
        try {
            System.out.println("Sleeping...");
            Thread.sleep(5000);
        }catch(InterruptedException e) { }

        long start = System.currentTimeMillis();
        ArrayList<String> titles = new ArrayList<>();

        ArrayList<String> list2017 = new ArrayList<>();
        ArrayList<String> list2018 = new ArrayList<>();
        ArrayList<String> checkFail = new ArrayList<>();

        JSONParser parser = new JSONParser();
        JSONObject current = new JSONObject();
        JSONObject upcoming = new JSONObject();

        int total = 0, failed = 0, pass = 0, cur = 0, upc = 0;
        try {
            Scanner scanner = new Scanner(new File("movies.txt"));

            while (scanner.hasNextLine())
                titles.add(scanner.nextLine());

            scanner.close();
        }catch(FileNotFoundException err) {
            System.out.println("File Not Found: " + err.getMessage());
        }

        System.out.println("\nDone reading titles - " + titles.size() + " entries\n");


        for(int index = titles.size() - 1; index >=0 && run; index--) {
            String id = titles.get(index);

            if(total % 50 == 0)
                System.out.println("\nTotal = " + total + ", Failed = " + failed + ", Movies = " + pass + ", Current = " + cur + ", Upcoming = " + upc);

            try {
                String res = get(id);
                Object tmpObj = parser.parse(res);
                JSONObject tempJson = (JSONObject) tmpObj;

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


                if(tempJson.get("Response").toString().equalsIgnoreCase("True") & !tempJson.get("Title").toString().equalsIgnoreCase("N/A")) {

                    if(tempJson.get("Year").toString().equalsIgnoreCase("2017")) {
                        list2017.add(id);
                        if(rate && rel && run && gen && dir && act && plot && lang && coun && post && rati && box) {
                            current.put(id, tmpObj);
                            pass++;
                            cur++;
                        }
                        else {
                            checkFail.add(id);
                            failed++;
                        }
                    }
                    else if(tempJson.get("Year").toString().equalsIgnoreCase("2018")) {
                        list2018.add(id);
                        if(rel && gen && dir && act && plot && lang && coun && post) {
                            upcoming.put(id, tmpObj);
                            pass++;
                            upc++;
                        }
                        else {
                            checkFail.add(id);
                            failed++;
                        }
                    }
                    else
                        failed++;
                }
                else {
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
            long end = System.currentTimeMillis();
            long diff = end - start;
            System.out.println("\nTime elapsed = " + (diff / (60 * 1000) % 60) +  "mins  " + diff / 1000 % 60 + " secs");
            System.out.println("Total = " + total + ", Failed = " + failed + ", Movies = " + pass + ", Current = " + cur + ", Upcoming = " + upc);

            FileWriter p1 = new FileWriter("current.json");
            FileWriter p2 = new FileWriter("upcoming.json");

            PrintWriter p3 = new PrintWriter(new File("list2017.txt"));
            PrintWriter p4 = new PrintWriter(new File("list2018.txt"));
            PrintWriter p5 = new PrintWriter(new File("checkFail.txt"));

            p1.write(current.toJSONString());
            p2.write(upcoming.toJSONString());

            for(int i = 0; i < list2017.size(); i++)
                p3.println(list2017.get(i));

            for(int i = 0; i < list2018.size(); i++)
                p4.println(list2018.get(i));

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

    public static String get(String id) throws Exception {
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

    public static void doThread() {
        Thread t = new Thread();

    }


    public static void main(String[] args) {
        makeIDS();

        Exit one = new Exit();
        Thread t1 = new Thread(one);
        t1.setDaemon(true);
        t1.start();

        doPosts();

    }
}
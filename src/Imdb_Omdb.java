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

    private static ArrayList<String> titles = new ArrayList<>();
    private static ArrayList<String> checkFail = new ArrayList<>();

    private static JSONParser parser = new JSONParser();
    private static JSONObject current = new JSONObject();
    private static JSONObject upcoming = new JSONObject();

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

    public static void removeOldFailed() {
        long start = System.currentTimeMillis();
        try {
            System.out.println("Going to remove old failed titles...");
            Thread.sleep(2500);
        }catch(InterruptedException e) { }

        try {
            Scanner scanner1 = new Scanner(new File("movies.txt"));
            Scanner scanner2 = new Scanner(new File("files/checkFail.txt"));

            while (scanner1.hasNextLine())
                titles.add(scanner1.nextLine());

            while (scanner2.hasNextLine())
                checkFail.add(scanner2.nextLine());

            scanner1.close();
            scanner2.close();
            System.out.println("All Movies - " + titles.size() + ", Failed Movies - " + checkFail.size());

            for(int i = 0; i < titles.size(); i++) {
                String title = titles.get(i);
                for(int j = 0; j < checkFail.size(); j++) {
                    if(title.equalsIgnoreCase(checkFail.get(j))) {
                        titles.remove(i);
                        break;
                    }
                }
            }

            long end = System.currentTimeMillis();
            long diff = end - start;
            System.out.println("Time elapsed = " + ((diff / 1000) / 3600) +  "hrs  " + ((diff / 1000) / 60) +  "mins  " + ((diff / 1000) % 60) + " secs");
        }catch(FileNotFoundException err) {
            System.out.println("File Not Found: " + err.getMessage());
        }
    }

    public static void doPosts() {
        long start = System.currentTimeMillis();
        System.out.println("\nStarting doPosts... " + titles.size() + " entries\n");

        ArrayList<String> list2017 = new ArrayList<>();
        ArrayList<String> list2018 = new ArrayList<>();

        int total = 0, failed = 0, pass = 0, cur = 0, upc = 0;

        for(int index = titles.size() - 1; index >=0 && run; index--) {
            String id = titles.get(index);

            if(total % 200 == 0)
                System.out.println("Done = " + total + ", Left = " + (titles.size() - total) + ", Passed = " + pass + ", Failed = " + failed + ", Current = " + cur + ", Upcoming = " + upc);

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


                if(tempJson.get("Response").toString().equalsIgnoreCase("True") && !tempJson.get("Title").toString().equalsIgnoreCase("N/A"))
                {
                    if(tempJson.get("Year").toString().equalsIgnoreCase("2017")) {
                        if(rate && rel && run && gen && dir && act && plot && lang && coun && post && rati && box) {
                            current.put(id, tmpObj);
                            list2017.add(id);
                            pass++;
                            cur++;
                        }
                        else {
                            checkFail.add(id);
                            failed++;
                        }
                    }
                    else {
                        if(rel && gen && dir && act && plot && lang && coun && post)
                        {
                            if(checkUpcoming(tempJson.get("Released").toString()))
                            {
                                upcoming.put(id, tmpObj);
                                list2018.add(id);
                                pass++;
                                upc++;
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
                    }
                }
                else {
                    checkFail.add(id);
                    failed++;
                }

            }catch(ParseException e) {
                checkFail.add(id);
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
            long hrs = (diff / 1000) / 3600;
            long mins = ((diff - (hrs * 3600 * 1000)) / 1000) / 60;
            System.out.println("\nTime elapsed = " + hrs +  "hrs  " + mins +  "mins  " + ((diff / 1000) % 60) + " secs");
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

    private static boolean checkUpcoming(String released) {
        String lower = released.toLowerCase();
        if(lower.contains("2018"))
            return true;
        else if(lower.contains("2019"))
            return true;
        else if(lower.contains("2020"))
            return true;
        else if(lower.contains("2021"))
            return true;
        else if(lower.contains("2022"))
            return true;
        else if(lower.contains("2023"))
            return true;
        else if(lower.contains("2024"))
            return true;
        else if(lower.contains("2025"))
            return true;
        else
            return false;
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

    public static void main(String[] args) {
        makeIDS();

        Exit one = new Exit(true);
        Thread t1 = new Thread(one);
        t1.setDaemon(true);
        t1.start();

        doPosts();
    }
}
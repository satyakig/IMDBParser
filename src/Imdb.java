import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import java.io.*;
import java.util.*;
import java.net.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Imdb {
    public static volatile boolean run = true;

    public static void makeIDS() {
        TsvParserSettings settings = new TsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        TsvParser parser = new TsvParser(settings);

        List<String[]> allRows;
        ArrayList<String> titleID = new ArrayList<>();

        try {
            allRows = parser.parseAll(new FileReader("data.tsv"));

            System.out.println(allRows.size() + "\n");
            for(int i = allRows.size() - 1; i > 0; i--) {
                try {
                    String id = allRows.get(i)[0];
                    String type = allRows.get(i)[1];
                    int year = 0;
                    year = Integer.parseInt(allRows.get(i)[5]);

                    if(type.equals("movie") && (year == 2017 || year == 2018)) {
                        System.out.println(i + " - " + allRows.get(i)[2] + " " + year);
                        titleID.add(id);
                    }
                    else
                        System.out.println(i + " - " + " " + year);
                }catch(NumberFormatException e) { }
            }

            PrintWriter printer = new PrintWriter(new File("newHello.txt"));
            for(int i = 1; i < titleID.size(); i++)
                printer.println(titleID.get(i));
            printer.close();

            System.out.println("Done MakeIDs\n\n");

        }catch(FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void doPosts() {
        ArrayList<String> allId = new ArrayList<>();

        ArrayList<String> totalFail = new ArrayList<>();
        ArrayList<String> checkFail = new ArrayList<>();

        JSONParser parser = new JSONParser();
        JSONObject current = new JSONObject();
        JSONObject upcoming = new JSONObject();

        Scanner in = new Scanner(System.in);


        int total = 0, failed = 0, pass = 0, cur = 0, upc = 0;
        try {
            Scanner scanner = new Scanner(new File("newHello.txt"));

            while (scanner.hasNextLine())
                allId.add(scanner.nextLine());
        }catch(FileNotFoundException err) {
            System.out.println("File Not Found: " + err.getMessage());
        }


        for(int index = allId.size() - 1; index >=0 && run; index--) {
            String id = allId.get(index);

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
                        if(rate && rel  && run && gen && dir && act && plot && lang && coun && post && rati && box) {
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
                        if(gen && dir && act && plot && lang && coun && post) {
                            upcoming.put(id, tmpObj);
                            pass++;
                            upc++;
                        }
                        else {
                            checkFail.add(id);
                            failed++;
                        }
                    }
                    else {
                        totalFail.add(id);
                        failed++;
                    }
                }
                else {
                    totalFail.add(id);
                    failed++;
                }

            }catch(ParseException e) {
                totalFail.add(id);
                failed++;
            }catch(Exception e) {
                totalFail.add(id);
                failed++;
            }
            total++;
        }
        run = false;

        try {
            System.out.println("\nTotal = " + total + ", Failed = " + failed + ", Movies = " + pass + ", Current = " + cur + ", Upcoming = " + upc);
            FileWriter p1 = new FileWriter("current.json");
            FileWriter p2 = new FileWriter("upcoming.json");

            PrintWriter p3 = new PrintWriter(new File("totalFail.txt"));
            PrintWriter p4 = new PrintWriter(new File("checkFail.txt"));

            p1.write(current.toJSONString());
            p2.write(upcoming.toJSONString());

            for(int i = 0; i < totalFail.size(); i++)
                p3.println(totalFail.get(i));

            for(int i = 0; i < checkFail.size(); i++)
                p4.println(checkFail.get(i));

            p1.close();
            p2.close();
            p3.close();
            p4.close();

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
//        makeIDS();

        Runab one = new Runab();
        Thread t1 = new Thread(one);
        t1.start();

        doPosts();
    }
}
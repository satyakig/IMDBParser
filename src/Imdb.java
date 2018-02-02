import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import java.io.*;
import java.util.*;
import java.net.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Imdb {

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
        JSONArray arr = new JSONArray();
        JSONParser parser = new JSONParser();
        JSONObject totalObject = new JSONObject();

        ArrayList<String> fail1 = new ArrayList<>();
        ArrayList<String> fail2 = new ArrayList<>();

        ArrayList<String> allId = new ArrayList<>();

        int total = 0, pass = 0, failed = 0;
        try {
            Scanner scanner = new Scanner(new File("newHello.txt"));

            while (scanner.hasNextLine())
                allId.add(scanner.nextLine());
        }catch(FileNotFoundException err) {
            System.out.println("File Not Found: " + err.getMessage());
        }

        for(int index = allId.size() - 1; index >=0 && pass < 500; index--) {
            String id = allId.get(index);
            if(total % 100 == 0)
                System.out.println("Total: " + total + ", Pass: " + pass + ", Fail: " + failed);

            try {
                String res = get(id);
                Object tmpObj = parser.parse(res);
                JSONObject tempJson = (JSONObject) tmpObj;

                boolean a = tempJson.get("Response").toString().equalsIgnoreCase("True");
                boolean b = !tempJson.get("Title").toString().equalsIgnoreCase("N/A");
                boolean c = !tempJson.get("Year").toString().equalsIgnoreCase("N/A") && (tempJson.get("Year").toString().equalsIgnoreCase("2017") || tempJson.get("Year").toString().equalsIgnoreCase("2018"));
                boolean d = !tempJson.get("Released").toString().equalsIgnoreCase("N/A");
                boolean e = !tempJson.get("Genre").toString().equalsIgnoreCase("N/A");
                boolean f = !tempJson.get("Director").toString().equalsIgnoreCase("N/A");
                boolean g = !tempJson.get("Actors").toString().equalsIgnoreCase("N/A");
                boolean h = !tempJson.get("Plot").toString().equalsIgnoreCase("N/A");
                boolean i = tempJson.get("Language").toString().contains("English");
                boolean j = tempJson.get("Country").toString().contains("USA");
                boolean k = !tempJson.get("Poster").toString().equalsIgnoreCase("N/A");

                if(a) {
                    if(b && c && d && e && f && g && h && i && j && k) {
                        totalObject.put(id, tmpObj);
                        arr.add(tmpObj);
                        pass++;
                    }
                    else {
                        fail2.add(id);
                        failed++;
                        System.out.println(id + " - Title:" + b + ", Year:" + c + ", Released:" + d + ", Genre:" + e + ", Director:" + f + ", Actors:" + g + ", Plot:" + h + ", Language:" + i + ", Country:" + j + ", Poster:" + k);
                    }
                }
                else {
                    fail1.add(id);
                    failed++;
                }

            }catch(ParseException e) {
                fail2.add(id);
                failed++;
                System.out.println(total + " ParseException: " + e.getMessage());
            }catch(Exception e) {
                fail1.add(id);
                failed++;
            }
            total++;
        }

        try {
            System.out.println("IDs read = " + total + "\nMovies made = " + pass + "\nMovies failed = " + failed);
            FileWriter p1 = new FileWriter("arr.json");
            FileWriter p2 = new FileWriter("obj.json");

            PrintWriter p3 = new PrintWriter(new File("fail1.txt"));
            PrintWriter p4 = new PrintWriter(new File("fail2.txt"));

            p1.write(arr.toJSONString());
            p2.write(totalObject.toJSONString());

            for(int i = 0; i < fail1.size(); i++)
                p3.println(fail1.get(i));

            for(int i = 0; i < fail2.size(); i++)
                p4.println(fail2.get(i));

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


    public static void main(String[] args) {
//        makeIDS();
        doPosts();
    }
}
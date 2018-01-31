import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import java.io.*;
import java.util.*;
import java.net.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

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
            for(int i = allRows.size() - 1; i > 4700000; i--) {
                try {
                    String id = allRows.get(i)[0];
                    String type = allRows.get(i)[1];
                    int year = 0;
                    year = Integer.parseInt(allRows.get(i)[5]);

                    if(type.equals("movie") && year == 2017) {
                        System.out.println(i + " - " + allRows.get(i)[2] + " " + year);
                        titleID.add(id);
                    }
                    else {
                        System.out.println(i + " - " + " " + year);
                    }
                }catch(NumberFormatException e) { }
            }

            PrintWriter printer = new PrintWriter(new File("hello2.txt"));
            for(int i = 1; i < titleID.size(); i++)
                printer.println(titleID.get(i));
            printer.close();

        }catch(FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void doPosts() {
        JSONArray arr = new JSONArray();
        JSONParser parser = new JSONParser();
        JSONObject totalObject = new JSONObject();

        try {
            Scanner scanner = new Scanner(new File("hello.txt"));

            int i = 0;
            while(scanner.hasNextLine()) {
                System.out.println(i);
                String id = scanner.nextLine();
                String res = get(id);

                Object tmpObj = parser.parse(res);
                JSONObject tempJson = (JSONObject) tmpObj;


                if((tempJson.get("Response").equals("True") || tempJson.get("Response").equals("true")) && (tempJson.get("Language").equals("English") || tempJson.get("Language").equals("english")) && !(tempJson.get("imdbRating").equals("N/A"))) {
                    totalObject.put(id, tmpObj);
                    arr.add(tmpObj);
                }
                else
                    System.out.println("False " + id);

                i++;
            }

        }catch(FileNotFoundException err) {
            System.out.println(err.getMessage());
        }
        catch(Exception err) {
            System.out.println(err.getMessage());
        }

        try {
            FileWriter p1 = new FileWriter("a1.json");
            FileWriter p2 = new FileWriter("a2.json");
            p1.write(arr.toJSONString());
            p2.write(totalObject.toJSONString());

            p1.close();
            p2.close();

        }catch(Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public static String get(String id) throws Exception {
        String url = "http://www.omdbapi.com/?i=" + id + "&apikey=8aebf5c5";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("charset", "utf-8");

        int responseCode = con.getResponseCode();
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
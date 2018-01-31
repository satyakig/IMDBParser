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

        }catch(FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void doPosts() {
        JSONArray arr = new JSONArray();
        JSONParser parser = new JSONParser();
        JSONObject totalObject = new JSONObject();
        int i = 0, j = 0;

        try {
            Scanner scanner = new Scanner(new File("newHello.txt"));

            while(scanner.hasNextLine()) {
                try {
                    String id = scanner.nextLine();
                    String res = get(id);

                    Object tmpObj = parser.parse(res);
                    JSONObject tempJson = (JSONObject) tmpObj;


                    if(tempJson.get("Response").equals("True") || tempJson.get("Response").equals("true")) {
                        if(tempJson.get("Language").equals("English") || tempJson.get("Language").equals("english")) {
                            if(tempJson.get("Country").equals("USA") || tempJson.get("Language").equals("usa")) {
                                totalObject.put(id, tmpObj);
                                arr.add(tmpObj);
                                j++;
                            }
                            else
                                System.out.println(i + " - not usa " + id);
                        }
                        else
                            System.out.println(i + " - not english " + id);
                    }
                    else
                        System.out.println(i + " - false " + id);
                }catch(ParseException e) {
                    System.out.println(i + " IOException: " + e.getMessage());
                }catch(Exception e) {
                    System.out.println(i + " HTTPException: " + e.getMessage());
                }
                i++;
            }

            try {
                System.out.println("IDs read = " + i + "\nMovies made = " + j);
                FileWriter p1 = new FileWriter("arr.json");
                FileWriter p2 = new FileWriter("obj.json");
                p1.write(arr.toJSONString());
                p2.write(totalObject.toJSONString());

                p1.close();
                p2.close();

            }catch(IOException e) {
                e.printStackTrace();
                System.out.println("IOException: " + e.getMessage());
            }

        }catch(FileNotFoundException err) {
            System.out.println("File Exception: " + err.getMessage());
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
        makeIDS();
        doPosts();
    }
}
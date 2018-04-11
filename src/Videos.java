import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONArray;

import java.io.*;
import java.util.*;
import java.net.*;


public class Videos {
    public volatile boolean run = true;

    private ArrayList<String> currentIDs;
    private ArrayList<String> upcomingIDs;

    private JSONParser parser;
    private JSONArray current;
    private JSONArray upcoming;

    public Videos() {
        currentIDs = new ArrayList<>();
        upcomingIDs = new ArrayList<>();

        parser = new JSONParser();
        current = new JSONArray();
        upcoming = new JSONArray();
    }

    public void start() {
        long start = System.currentTimeMillis();

        Exit one = new Exit(this);
        Thread t1 = new Thread(one);
        t1.setDaemon(true);
        t1.start();

        this.readIDs();
        this.makeJson();

        long end = System.currentTimeMillis();
        long diff = end - start;
        long hrs = (diff / 1000) / 3600;
        long mins = ((diff - (hrs * 3600 * 1000)) / 1000) / 60;
        System.out.println("\nTime elapsed for Videos = " + hrs +  "hrs  " + mins +  "mins  " + ((diff / 1000) % 60) + " secs");
    }

    private void readIDs() {
        currentIDs = new ArrayList<>();
        upcomingIDs = new ArrayList<>();

        try {
            Scanner scanner1 = new Scanner(new File("currentMovies.txt"));
            Scanner scanner2 = new Scanner(new File("upcomingMovies.txt"));

            while(scanner1.hasNextLine())
                currentIDs.add(scanner1.nextLine());
            while(scanner2.hasNextLine())
                upcomingIDs.add(scanner2.nextLine());

            scanner1.close();
            scanner2.close();
        }catch(FileNotFoundException err) {
            System.out.println(err.getMessage());
        }
        System.out.println("\nStarting making videos... Current - " + currentIDs.size() + ", Upcoming" + upcomingIDs.size());
    }

    private void makeJson() {
        for(int i = 0; i < currentIDs.size() && run; i++) {
            String id = currentIDs.get(i);

            try {
                Thread.sleep(300);
                String respose = getMovie(id);
                JSONObject obj = (JSONObject) parser.parse(respose);

                JSONArray arr = (JSONArray) obj.get("movie_results");
                JSONObject arrObj = (JSONObject) arr.get(0);

                String tmdbID = arrObj.get("id").toString();

                if(tmdbID != null) {
                    Thread.sleep(300);
                    String secRes = getVideos(tmdbID);

                    JSONObject secObj = (JSONObject) parser.parse(secRes);
                    JSONArray videos = (JSONArray) secObj.get("results");

                    if(videos != null && videos.size() != 0) {
                        JSONObject tmpObj = new JSONObject();
                        tmpObj.put("id", id);
                        tmpObj.put("videos", videos);
                        current.add(tmpObj);
                    }
                }
            }catch(Exception err) {
                System.out.println(id + " cur, " + err.getMessage());
            }
        }

        System.out.println("\nDone current movies...\n");

        for(int i = 0; i < upcomingIDs.size() && run; i++) {
            String id = upcomingIDs.get(i);

            try {
                Thread.sleep(300);
                String respose = getMovie(id);
                JSONObject obj = (JSONObject) parser.parse(respose);

                JSONArray arr = (JSONArray) obj.get("movie_results");
                JSONObject arrObj = (JSONObject) arr.get(0);

                String tmdbID = arrObj.get("id").toString();

                if(tmdbID != null) {
                    Thread.sleep(300);
                    String secRes = getVideos(tmdbID);

                    JSONObject secObj = (JSONObject) parser.parse(secRes);
                    JSONArray videos = (JSONArray) secObj.get("results");

                    if(videos != null && videos.size() != 0) {
                        JSONObject tmpObj = new JSONObject();
                        tmpObj.put("id", id);
                        tmpObj.put("videos", videos);
                        upcoming.add(tmpObj);
                    }
                }
            }catch(Exception err) {
                System.out.println(id + " upc, " + err.getMessage());
            }
        }
        run = false;

        try {
            FileWriter p1 = new FileWriter("current-videos.json");
            FileWriter p2 = new FileWriter("upcoming-videos.json");

            p1.write(current.toJSONString());
            p2.write(upcoming.toJSONString());

            p1.close();
            p2.close();
        }catch(IOException e) {
            e.printStackTrace();
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private String getMovie(String id) throws Exception {

        String url = "https://api.themoviedb.org/3/find/" + id + "?api_key=f2f28a178514b22b8ed92869734bf6ac&external_source=imdb_id";
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

    private String getVideos(String id) throws Exception {

        String url = "https://api.themoviedb.org/3/movie/" + id + "/videos?api_key=f2f28a178514b22b8ed92869734bf6ac";
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
        Videos videos = new Videos();
        videos.start();
    }
}

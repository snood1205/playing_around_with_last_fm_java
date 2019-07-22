import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

import static java.lang.System.exit;

public class TrackFetcher {
    private String apiKey;
    private String username;
    private ArrayList<Track> tracks;
    private Date lastTime;

    public TrackFetcher(Date lastTime) {
        Dotenv dotenv = Dotenv.load();
        this.lastTime = lastTime;
        this.apiKey = dotenv.get("API_KEY");
        this.username = dotenv.get("USERNAME");
        this.tracks = new ArrayList<>();
    }

    public TrackFetcher() {
        // Use epoch if there is no date provided
        this(new Date(0));
    }

    public int fetchNewTracks() {
        int totalPages = fetchTotalPages();

        System.out.printf("Total pages fetched: %d\n", totalPages);
        boolean keepProcessing = true;
        for (int pageNumber = 1; keepProcessing && pageNumber <= totalPages; pageNumber++) {
            JSONArray trackArray = fetchTracks(pageNumber);
            keepProcessing = processTracks(trackArray);
        }
        return tracks.size();
    }

    // Fetchers

    private int fetchTotalPages(int retryCount) {
        System.out.println("fetching total pages...");
        int totalPages = 0;
        try {
            JSONObject obj = readJsonFromUrl(generateUrl(1));
            totalPages = obj.getJSONObject("recenttracks")
                    .getJSONObject("@attr")
                    .getInt("totalPages");
        } catch (IOException e) {
            if (retryCount < 5) {
                System.out.printf("Fetch retry number %d", ++retryCount);
                return fetchTotalPages(retryCount);
            } else {
                e.printStackTrace();
                System.err.println("Unable to fetch total pages");
                exit(2);
            }
        }
        return totalPages;
    }

    private int fetchTotalPages() {
        return fetchTotalPages(0);
    }

    private JSONArray fetchTracks(int pageNumber, int retryCount) {
        JSONArray tracks = null;
        System.out.printf("Fetching page %d\n", pageNumber);
        try {
            JSONObject obj = readJsonFromUrl(generateUrl(pageNumber));
            tracks = obj.getJSONObject("recenttracks").getJSONArray("track");
        } catch (IOException e) {
            if (retryCount < 5) {
                System.out.printf("Fetch retry number %d", ++retryCount);
                return fetchTracks(pageNumber, retryCount);
            } else {
                e.printStackTrace();
                System.err.printf("Unable to fetch page %d\n", pageNumber);
                exit(2);
            }
        }
        return tracks;
    }

    private JSONArray fetchTracks(int pageNumber) {
        return fetchTracks(pageNumber, 0);
    }

    // Processors

    private boolean processTracks(JSONArray trackArray) {
        int length = trackArray.length();
        boolean keepProcessing = true;
        for (int i = 0; keepProcessing && i < length; i++) {
            keepProcessing = processAndAppendTrack(trackArray.getJSONObject(i));
        }
        return keepProcessing;
    }

    private boolean processAndAppendTrack(JSONObject trackObject) {
        if (!trackObject.has("date"))
            return true;
        String artist = trackObject.getJSONObject("artist").getString("#text");
        String album = trackObject.getJSONObject("album").getString("#text");
        String imageUrl = trackObject.getJSONObject("image").getString("#text");
        long uts = trackObject.getJSONObject("date").getLong("uts");
        Date listenedAt = new Date(uts * 1000L);
        String name = trackObject.getString("name");
        String url = trackObject.getString("url");
        Track track = new Track(artist, album, name, listenedAt, imageUrl, url);
        boolean keepProcessing = !listenedAt.before(lastTime);
        if (keepProcessing) {
            tracks.add(track);
        }
        return keepProcessing;
    }

    // Helpers

    private String buildString(Reader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        int chr;
        while ((chr = reader.read()) != -1) builder.append((char) chr);
        return builder.toString();
    }

    private JSONObject readJsonFromUrl(URL url) throws IOException {
        // Largely adapted from:
        // https://stackoverflow.com/questions/4308554/simplest-way-to-read-json-from-a-url-in-java
        InputStream stream = url.openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        return new JSONObject(buildString(reader));
    }


    private URL generateUrl(int pageNumber) throws MalformedURLException {
        String str = String.format(
                "http://ws.audioscrobber.com/2.0/?method=user.getrecenttracks&user=%s&api_key=%s&format=json&page=%d",
                this.username, this.apiKey, pageNumber
        );
        return new URL(str);
    }
}

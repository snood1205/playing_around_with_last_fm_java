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
    private boolean keepProcessing;

    /**
     * Create a new track fetcher
     *
     * @param lastTime only fetch songs scrobbled after this time.
     */
    public TrackFetcher(Date lastTime) {
        Dotenv dotenv = Dotenv.load();
        this.lastTime = lastTime;
        this.apiKey = dotenv.get("API_KEY");
        this.username = dotenv.get("USERNAME");
        this.tracks = new ArrayList<>();
        this.keepProcessing = true;
    }

    /**
     * Create a new track fetcher. Fetches all songs scrobbled after epoch.
     */
    public TrackFetcher() {
        this(new Date(0));
    }

    /**
     * Fetch new tracks from the last.fm API.
     */
    public void fetchNewTracks() {
        int totalPages = fetchTotalPages();

        System.out.printf("Total pages fetched: %d\n", totalPages);
        for (int pageNumber = 1; keepProcessing && pageNumber <= totalPages; pageNumber++) {
            JSONArray trackArray = fetchTracks(pageNumber);
            processTracks(trackArray);
        }
    }

    // Fetchers

    /**
     * Fetches the total number of pages.
     *
     * @param retryCount which retry attempt this fetch attempt is on.
     * @return the total number of pages of a user's last.fm scrobbles.
     */
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

    /**
     * Fetches the total number of pages.
     *
     * @return the total number of pages of a user's last.fm scrobbles.
     */
    private int fetchTotalPages() {
        return fetchTotalPages(0);
    }

    /**
     * Fetch a page of tracks from the last.fm API.
     *
     * @param pageNumber the page number to fetch.
     * @param retryCount which retry attempt this fetch attempt is on.
     * @return an array of JSON track objects
     */
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

    /**
     * Fetch a page of tracks from the last.fm API.
     *
     * @param pageNumber the page number to fetch.
     * @return an array of JSON track objects
     */
    private JSONArray fetchTracks(int pageNumber) {
        return fetchTracks(pageNumber, 0);
    }

    // Processors

    /**
     * Processes a JSON array of tracks by appending the tracks fetched onto {@link #tracks}.
     *
     * @param trackArray an array of JSON track objects to parse.
     */
    private void processTracks(JSONArray trackArray) {
        int length = trackArray.length();
        for (int i = 0; keepProcessing && i < length; i++)
            processAndAppendTrack(trackArray.getJSONObject(i));
    }

    /**
     * Processes a JSON track object and append it to {@link #tracks}.
     *
     * @param trackObject the JSON track object to parse.
     */
    private void processAndAppendTrack(JSONObject trackObject) {
        if (!trackObject.has("date")) return;
        String artist = trackObject.getJSONObject("artist").getString("#text");
        String album = trackObject.getJSONObject("album").getString("#text");
        String imageUrl = trackObject.getJSONArray("image").getJSONObject(3).getString("#text");
        long uts = trackObject.getJSONObject("date").getLong("uts");
        Date listenedAt = new Date(uts * 1000L);
        String name = trackObject.getString("name");
        String url = trackObject.getString("url");
        Track track = new Track(artist, album, name, listenedAt, imageUrl, url);
        keepProcessing = !listenedAt.before(lastTime);
        if (keepProcessing) {
            tracks.add(track);
        }
    }

    // Dumpers (IO operations)

    /**
     * Dumps all of the tracks stored in {@link #tracks} in a JSON format to the provided writer.
     *
     * @param writer the writer to write the tracks to in JSON.
     */
    public void dumpTracks(PrintWriter writer) {
        JSONArray tracks = new JSONArray(this.tracks.stream().map(Track::toJsonObject).toArray());
        writer.println(tracks);
        writer.close();
    }

    // Helpers

    /**
     * Constructs a string from a reader.
     *
     * @param reader the reader from which to read the string.
     * @return the string version of the reader after being read until {@code EOF}.
     * @throws IOException thrown if the reader can't read properly.
     */
    private String buildString(Reader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        int chr;
        while ((chr = reader.read()) != -1) builder.append((char) chr);
        return builder.toString();
    }

    /**
     * Gets a JSON object from a URL.
     *
     * @param url the url from which to fetch and read the JSON object.
     * @return A JSON Object parsed from the provided endpoint.
     * @throws IOException thrown if the url can't open a stream or if the reader can't read properly.
     */
    private JSONObject readJsonFromUrl(URL url) throws IOException {
        // Largely adapted from:
        // https://stackoverflow.com/questions/4308554/simplest-way-to-read-json-from-a-url-in-java
        InputStream stream = url.openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        return new JSONObject(buildString(reader));
    }


    private URL generateUrl(int pageNumber) throws MalformedURLException {
        String str = String.format(
                "http://ws.audioscrobbler.com/2.0/?method=user.getrecenttracks&user=%s&api_key=%s&format=json&page=%d",
                this.username, this.apiKey, pageNumber
        );
        return new URL(str);
    }
}

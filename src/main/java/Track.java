import org.jetbrains.annotations.Contract;
import org.json.JSONObject;

import java.util.Date;
import java.util.GregorianCalendar;

public class Track {
    // Variable declarations
    /**
     * The name of the artist who performs the track (for example: Parquet Courts)
     */
    private String artist;

    /**
     * The album the track is on (for example: Wide Awake!)
     */
    private String album;

    /**
     * The name of the track (for example: Tenderness)
     */
    private String name;

    /**
     * The time at which the track was listened to.
     */
    private Date listenedAt;

    /**
     * The link to the image on last.fm.
     */
    private String imageUrl;

    /**
     * The link to the track's page on last.fm.
     */
    private String url;

    // Constructors

    /**
     * The full constructor for Track.
     *
     * @param artist     the name of the artist who performs the track (for example: Parquet Courts)
     * @param album      the album the track is on (for example: Wide Awake!)
     * @param name       the name of the song (for example: Tenderness)
     * @param listenedAt the time the song was listened to (for example: {@code new GregorianCalendar(2018, Calendar.DECEMBER, 31, 23, 59, 59).getDate();}).
     * @param imageUrl   the URL for the album art
     * @param url        the URL for the track on last.fm
     */
    public Track(String artist, String album, String name, Date listenedAt, String imageUrl, String url) {
        this.artist = artist;
        this.album = album;
        this.name = name;
        this.listenedAt = listenedAt;
        this.imageUrl = imageUrl;
        this.url = url;
    }

    /**
     * The full constructor for Track with Greogorian Calendar option for listenedAt
     *
     * @param artist     the name of the artist who performs the track (for example: Parquet Courts)
     * @param album      the album the song is on (for example: Wide Awake!)
     * @param name       the name of the song (for example: Tenderness)
     * @param listenedAt the time the song was listened to
     *                   (for example: {@code new GregorianCalendar(2018, Calendar.DECEMBER, 31, 23, 59, 59);})
     * @param imageUrl   the URL for the album art
     * @param url        the URL for the track on last.fm
     */
    public Track(String artist, String album, String name, GregorianCalendar listenedAt, String imageUrl, String url) {
        this(artist, album, name, listenedAt.getTime(), imageUrl, url);
    }

    // Getters and setters

    /**
     * Gets the artist of the song. (for example: Snail Mail)
     *
     * @return the name of the artist of the track.
     */
    public String getArtist() {
        return artist;
    }

    /**
     * Set the artist of a song
     *
     * @param artist the name of the artist of the song.
     */
    public void setArtist(String artist) {
        this.artist = artist;
    }

    /**
     * Gets the album of a song. (for example: Habit)
     *
     * @return the name of the album of the song.
     */
    public String getAlbum() {
        return album;
    }

    /**
     * Set the album of a song.
     *
     * @param album the name of the album of the song.
     */
    public void setAlbum(String album) {
        this.album = album;
    }

    /**
     * Gets the name of a song. (for example: Thinning).
     *
     * @return the name of the song.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of a song.
     *
     * @param name the name of the song.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the date the song was listened to.
     *
     * @return the date the song was listened to.
     */
    public Date getListenedAt() {
        return listenedAt;
    }

    /**
     * Gets the url of the track on last.fm.
     *
     * @return the url of the track on last.fm
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets the url for the album artwork.
     *
     * @return the url for the album artwork.
     */
    public String getImageUrl() {
        return imageUrl;
    }

    // Equality Methods

    /**
     * Checks for equality.
     *
     * @param o the object to check for equality
     * @return true if the two objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Track) {
            Track t = (Track) o;
            return t.getAlbum().equals(album) &&
                    t.getArtist().equals(artist) &&
                    t.getName().equals(name) &&
                    t.getListenedAt().equals(listenedAt);
        }
        return super.equals(o);
    }

    /**
     * A looser equality method. This checks only if the artist, album, and name are the same
     *
     * @param track the track to check for loose equality
     * @return whether or not the tracks are loosely equal
     */
    public boolean looseEquals(Track track) {
        return track.getAlbum().equals(album) &&
                track.getArtist().equals(artist) &&
                track.getName().equals(name);
    }

    /**
     * A looser equality method. This checks if the artist and name are the same. The album is considered or not
     * depending upon the value provided to {@code considerAlbum.}
     *
     * @param track         the track to check for loose equality
     * @param considerAlbum whether or not to consider the album in checking for loose equality
     * @return whether or not the tracks are loosely equal
     */
    public boolean looseEquals(Track track, boolean considerAlbum) {
        return considerAlbum ?
                looseEquals(track) :
                track.getArtist().equals(artist) && track.getName().equals(name);
    }

    // Conversion methods

    /**
     * Returns the Track in a JSON format.
     *
     * @return a track in JSON string format
     */
    public String toJson() {
        return toJsonObject().toString(2);
    }

    /**
     * Converts a Track to a {@link JSONObject}.
     *
     * @return the {@link JSONObject} form of the track.
     */
    public JSONObject toJsonObject() {
        return new JSONObject(this);
    }
}

import com.sun.deploy.security.SelectableSecurityManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;

public class Track {
    // Variable declarations
    private String artist;
    private String album;
    private String name;
    private Date listenedAt;
    private String imageUrl;
    private String url;

    // Constructors

    /**
     * The full constructor for Track.
     *
     * @param artist     the name of the artist who performs the track (e.g. Parquet Courts)
     * @param album      the album the song is on (e.g. Wide Awake!)
     * @param name       the name of the song (e.g. Tenderness)
     * @param listenedAt the time the song was listened to (e.g. <code>new GregorianCalendar(2018, Calendar.DECEMBER, 31, 23, 59, 59).getDate();</code>
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
     * @param artist     the name of the artist who performs the track (e.g. Parquet Courts)
     * @param album      the album the song is on (e.g. Wide Awake!)
     * @param name       the name of the song (e.g. Tenderness)
     * @param listenedAt the time the song was listened to (e.g. <code>new GregorianCalendar(2018, Calendar.DECEMBER, 31, 23, 59, 59);code>
     * @param imageUrl   the URL for the album art
     * @param url        the URL for the track on last.fm
     */
    public Track(String artist, String album, String name, GregorianCalendar listenedAt, String imageUrl, String url) {
        this(artist, album, name, listenedAt.getTime(), imageUrl, url);
    }

    // Getters and setters

    /**
     * Gets the artist of the song. (e.g. Snail Mail)
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
     * Gets the album of a song. (e.g. Habit)
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
     * Gets the name of a song. (e.g. Thinning).
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

    public String getUrl() {
        return url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

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
     * depending upon the value provided to <code>considerAlbum.</code>
     *
     * @param track         the track to check for loose equality
     * @param considerAlbum
     * @return
     */
    public boolean looseEquals(Track track, boolean considerAlbum) {
        return considerAlbum ?
                looseEquals(track) :
                track.getArtist().equals(artist) && track.getName().equals(name);
    }
}

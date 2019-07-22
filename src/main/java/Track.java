import org.json.JSONObject;

import java.sql.*;
import java.util.Date;
import java.util.GregorianCalendar;

public class Track {
    // Variable declarations
    /**
     * The name of the artist who performs the track (for example: Parquet Courts)
     */
    private final String artist;

    /**
     * The album the track is on (for example: Wide Awake!)
     */
    private final String album;

    /**
     * The name of the track (for example: Tenderness)
     */
    private final String name;

    /**
     * The time at which the track was listened to.
     */
    private final Date listenedAt;

    /**
     * The link to the image on last.fm.
     */
    private final String imageUrl;

    /**
     * The link to the track's page on last.fm.
     */
    private final String url;

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
     * Gets the album of a song. (for example: Habit)
     *
     * @return the name of the album of the song.
     */
    public String getAlbum() {
        return album;
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

    // SQL Methods
    public void insertToDatabase(PostgresConnection postgresConnection) throws SQLException {
        insertToDatabase(postgresConnection, 0);
    }


    public void insertToDatabase(PostgresConnection postgresConnection, int retryCount) throws SQLException {
        Connection connection = postgresConnection.getConnection();
        PreparedStatement statement = prepareInsertStatement(connection);
        int rowCount = statement.executeUpdate();
        statement.close();
        if (rowCount == 0) {
            System.out.printf("Insert retry number: %d\n", ++retryCount);
            insertToDatabase(postgresConnection, retryCount);
        }
    }

    public PreparedStatement prepareInsertStatement(Connection connection) throws SQLException {
        String query = "INSERT INTO \"tracks\" (\"artist\", \"album\", \"name\", \"listened_at\", \"created_at\"," +
                "\"updated_at\", \"url\", \"image_url\") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);
        java.sql.Date current = new java.sql.Date((new Date()).getTime());
        java.sql.Date sqlListenedAt = new java.sql.Date(listenedAt.getTime());
        statement.setString(1, artist);
        statement.setString(2, album);
        statement.setString(3, name);
        statement.setDate(4, sqlListenedAt);
        statement.setDate(5, current);
        statement.setDate(6, current);
        statement.setString(7, url);
        statement.setString(8, imageUrl);
        return statement;
    }
}

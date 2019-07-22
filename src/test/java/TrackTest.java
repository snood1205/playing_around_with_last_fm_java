import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;

public class TrackTest {
    @Test
    public void whenAllTheParametersAreProperlyProvidedToFullConstructor() {
        String artist = "Nana Grizol";
        String album = "Love It Love It";
        String name = "Circles 'Round the Moon";
        Date listenedAt = new GregorianCalendar(2019, Calendar.JULY, 16, 13, 18, 12).getTime();
        String url = "https://www.last.fm/music/Nana+Grizol/_/Circles+%27Round+the+Moon";
        String imageUrl = "https://lastfm-img2.akamaized.net/i/u/300x300/2343f2d43d3f440ea9e5d5d75f032524.png";
        Track track = new Track(artist, album, name, listenedAt, imageUrl, url);
        assertEquals(artist, track.getArtist());
        assertEquals(album, track.getAlbum());
        assertEquals(name, track.getName());
        assertEquals(listenedAt, track.getListenedAt());
        assertEquals(url, track.getUrl());
        assertEquals(imageUrl, track.getImageUrl());
    }

    @Test
    public void whenAllTheParametersAreProperlyProvidedToFullConstructorWithGregorianCalendar() {
        String artist = "Nana Grizol";
        String album = "Love It Love It";
        String name = "Circles 'Round the Moon";
        GregorianCalendar listenedAt = new GregorianCalendar(2019, Calendar.JULY, 16, 13, 18, 12);
        String url = "https://www.last.fm/music/Nana+Grizol/_/Circles+%27Round+the+Moon";
        String imageUrl = "https://lastfm-img2.akamaized.net/i/u/300x300/2343f2d43d3f440ea9e5d5d75f032524.png";
        Track track = new Track(artist, album, name, listenedAt, imageUrl, url);
        assertEquals(artist, track.getArtist());
        assertEquals(album, track.getAlbum());
        assertEquals(name, track.getName());
        assertEquals(listenedAt.getTime(), track.getListenedAt());
        assertEquals(url, track.getUrl());
        assertEquals(imageUrl, track.getImageUrl());
    }
}
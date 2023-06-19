import java.util.Objects;

/**
 * <b>May not add any accessor/mutator for this class</b>
 */
public class PlayableItem implements Comparable<PlayableItem> {
    private int lastPlayedTime; // where user stopped in prev song in SECONDS
    private int totalPlayTime;
    private String endpoint; // the path to the file of the song, ex) /1.mp4
    private String title;
    private String artist;
    private int popularity; // the popularity level of this song (0 - 100)
    private int playedCounts; // How many times this song has been played, initially to be 0

    public PlayableItem(int lastTime, int totalPlayTime, String endpoint, String title,
                        String artist, int popularity) {
        this.lastPlayedTime = lastTime;
        this.totalPlayTime = totalPlayTime;
        this.endpoint = endpoint;
        this.title = title;
        this.artist = artist;
        this.popularity = popularity;
        this.playedCounts = 0;
    }

    public String getArtist() {
        return this.artist;
    }

    public String getTitle() {
        return this.title;
    }

    public int getPopularity() {
        return this.popularity;
    }

    public void setPopularity(int pop) {
        this.popularity = pop;
    }

    public boolean playable() {
        return this.lastPlayedTime != this.totalPlayTime;
    }

    public boolean play() {
        this.lastPlayedTime++;

        if (!this.playable()) {
            return false;
        }
        if (this.lastPlayedTime > this.totalPlayTime) {
            this.lastPlayedTime = 0;
            this.playedCounts++;
            return true;
        }

        return true;
    }

    public boolean equals(PlayableItem another) {
        // make sure title, artist, totalTime, endpoint same
        return Objects.equals(another.title, this.title)
                && Objects.equals(another.artist, this.artist)
                && Objects.equals(another.endpoint, this.endpoint)
                && another.totalPlayTime == this.totalPlayTime;
    }

    public String toString() {
        // title, endpoint, lastPlayedTime, totalPlayTime, artist, popularity, playedCounts
        return this.title + "," + this.endpoint + "," + this.lastPlayedTime
                + "," + this.totalPlayTime + "," + this.artist + "," + this.popularity
                + "," + this.playedCounts;
    }

    @Override
    public int compareTo(PlayableItem o) {
        if (this.playedCounts < o.playedCounts) {
            return -1;
        } else if (this.playedCounts > o.playedCounts) {
            return 1;
        }
        return 0;
    }
}

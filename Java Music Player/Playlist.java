import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Random;

public class Playlist {

    private String name;
    private int playingMode = 0;
    private int playingIndex = 0; // for inorder/random playing mode/artist recommended playing mode
    private ArrayList<PlayableItem> curList;
    // temp list for inorder/random playing mode/artist recommended playing mode
    private PlayableItem cur; // current playing element
    private Stack<PlayableItem> history;
    private PriorityQueue<PlayableItem> freqListened; // Java version of min-heap
    private ArrayList<PlayableItem> playlist; // contain the playlist being displayed

    public Playlist() {
        this.curList = new ArrayList<>();
        this.history = new Stack<>();
        this.freqListened = new PriorityQueue<>();
        this.playlist = new ArrayList<>();
        this.cur = null;
        this.name = "Default";
    }

    public Playlist(String name) {
        this();
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int size() {
        // total number of songs in playlist
        return this.playlist.size();
    }

    public String toString() {
        return this.getName() + "," + this.size() + " songs";
    }

    public void addPlayableItem(PlayableItem newItem) {
        this.playlist.add(newItem);
        // add location based on playback mode. This playlist permits duplicates.
        // new PlayableItem played when the playlist reaches this song under current playing mode.

        // if playback mode is 0: normal --> add to end of playlist
        if (this.playingMode == 0) {
            this.curList.add(newItem);
        }

        // if playback mode is 1: random --> add in song randomly
        if (this.playingMode == 1) {
            int randomIDX = new Random().nextInt(this.playlist.size());
            //int randomIDX = (int) (Math.random() * this.playlist.size());
            this.curList.add(randomIDX, newItem);
        }

        // if playback mode is 2: most frequently --> add to end of playlist, based off freqListened
        if (this.playingMode == 2) {
            this.freqListened.offer(newItem);
            this.curList.clear();
            this.curList.addAll(this.freqListened);
        }

        // if playback mode is 3: recommendation mode --> add to end of playlist
        if (this.playingMode == 3) {
            this.curList.add(newItem);
        }
    }

    public void addPlayableItem(ArrayList<PlayableItem> newItem) {
        for (PlayableItem item : newItem) {
            addPlayableItem(item);
        }
    }

    public boolean removePlayableItem(int number) {
        int removeIDX = number - 1;

        // if number entered is invalid
        if (removeIDX < 0 || removeIDX > (this.playlist.size() - 1)) {
            return false;
        }

        PlayableItem songToRemove = this.playlist.get(removeIDX);
        this.playlist.remove(songToRemove);
        this.curList.remove(songToRemove);
        this.history.remove(songToRemove);
        this.freqListened.remove(songToRemove);
        return true;
    }

    public void switchPlayingMode(int newMode) {

        if (this.playingMode == newMode) {
            return;
        }

        this.playingMode = newMode;

        this.curList.clear();
        this.history.clear();
        this.freqListened.clear();
        this.playingIndex = 0;

        // if playback mode is 0: normal
        if (newMode == 0) {
            this.curList.addAll(this.playlist);
        }

        // if playback mode is 1: random
        if (newMode == 1) {
            this.curList.addAll(this.playlist);
            Collections.shuffle(this.curList);
        }

        // if playback mode is 2: most frequently
        if (newMode == 2) {
            // adding all to freqListened
            for (PlayableItem item : this.playlist) {
                this.freqListened.offer(item);
            }

            // updating curList to freqListened
            this.curList.addAll(this.freqListened);
        }

        // if playback mode is 3: recommendation mode (add 10 recommended songs)
        if (newMode == 3) {
            this.curList.addAll(this.playlist);
        }
    }

    public ArrayList<String> getFiveMostPopular() {
        // Returns top 5 unique artists with songs that have the highest playedCounts

        // freqListened: a queue which contains songs that are ordered from least to most played
        // songs could be from SAME ARTIST

        // gets unique artists
        HashSet<String> uniqueArtists = new HashSet<>();
        while (!freqListened.isEmpty() && uniqueArtists.size() < 5) {
            PlayableItem song = freqListened.poll();
            uniqueArtists.add(song.getArtist());
        }

        ArrayList<String> artistList = new ArrayList<>(uniqueArtists);

        // if more than 5 unique artists, sort names alphabetically and choose first 5
        if (artistList.size() > 5) {
            Collections.sort(artistList);
            artistList = new ArrayList<>(artistList.subList(0, 5));
        }

        return artistList;
    }

    /**
     * Go to the last playing item
     */
    public void goBack() {
        if (this.history.size() == 0) {
            System.out.println("No more step to go back");
        } else {
            //this.history.pop();
            this.cur = this.history.pop();
            this.cur.play();
            this.curList.add(0, this.cur);
        }
    }

    public void play(int seconds) {
        //For play, go to next available song in the current playing mode

        // edge cases
        if (seconds <= 0 && this.curList.size() == 0) {
            System.out.println("Invalid seconds");
            return;
        }

        //kinda works on single song

        // adding current playlist elements to queue
        Queue<PlayableItem> playlistQueue = new LinkedList<PlayableItem>(this.curList);

        //prints "Seconds []: video1 start/complete."
        int sec = 0;
        while (playlistQueue.size() > 0) {
            PlayableItem song = playlistQueue.peek();
            this.cur = song;
            String songName = song.getTitle();

            String info = String.format("Seconds %d : %s start.", sec, songName);
            System.out.println(info);

            while (sec < seconds) {
                sec++;
                song.play(); //this.lastPlayedTime++

                if (!(song.playable())) {
                    String moreInfo = String.format("Seconds %d : %s complete.", sec, songName);
                    System.out.println(moreInfo);
                    this.history.add(song);
                    playlistQueue.remove(song);
                    this.curList.remove(song);

                    break;
                }
            }

            if ((sec == seconds) && song.playable()) {
                return;
            }
        }

        // if reaches end of current playlist
        this.playingIndex = 0;
        this.cur = null;
        System.out.println("No more music to play.");
    }

    public String showPlaylistStatus() {
        int songCounter = 0;
        String songPrintList = "";

        for (PlayableItem song : this.playlist) {
            songCounter++;
            songPrintList += songCounter + ". " + song.toString() + "\n";

            // currently playing
            if (this.cur != null && song.equals(this.cur)) {
                songPrintList = songPrintList.trim();
                songPrintList += " - Currently play" + "\n";
            }
        }

        // removing newline at end of the string
        return songPrintList.trim();
    }

    public PlayableItem getNextPlayable() {
        // frequency mode
        if (this.playingMode == 2) {
            return freqListened.poll();
        } else {
            if (this.playingIndex < curList.size()) {
                this.playingIndex++;
                return curList.get(this.playingIndex);
            }
        }
        return null;
    }

}

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;
import java.util.Arrays;

public class MusicDatabase {

    private Hashtable<String, ArrayList<PlayableItem>> data;
    private TreeMap<String, ArrayList<PlayableItem>> artists;
    private Recommendation recommender;
    private int size;

    // added private variables

    public MusicDatabase() {
        data = new Hashtable<>();
        artists = new TreeMap<>();
        this.size = 0;
    }

    public boolean addSongs(File inputFile) {
        // reading file
        List<List<String>> rawSongsList = new ArrayList<>();

        try {
            FileReader inputFr = new FileReader(inputFile);
            BufferedReader inputBr = new BufferedReader(inputFr);
            String line;

            // reads the first line to skip it
            inputBr.readLine();

            while ((line = inputBr.readLine()) != null) {
                String[] values = line.split(",");
                rawSongsList.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            return false;
        }

        //System.out.println(rawSongsList);

        for (List<String> songAttributes : rawSongsList) {
            int num = Integer.parseInt(songAttributes.get(0));
            String playlist = songAttributes.get(1);
            String name = songAttributes.get(2);
            String artist = songAttributes.get(3);
            int duration = Integer.parseInt(songAttributes.get(4)); // already in SECONDS
            int popularity = Integer.parseInt(songAttributes.get(5));
            String instrumental = songAttributes.get(6); // prob not important
            String endpoint = songAttributes.get(7);

            addSongs(name, artist, duration, popularity, endpoint);
        }

        return true;
    }

    public void addSongs(String name, String artist, int duration, int popularity,
                         String endpoint) {
        PlayableItem newSong = new PlayableItem(0, duration, endpoint, name,
                artist, popularity);
        //System.out.println(newSong);

        // adds newSong to data
        // check if song name exists in data
        if (data.containsKey(name)) {
            boolean updatePop = false;

            ArrayList<PlayableItem> specificTitle = data.get(name);
            for (PlayableItem song : specificTitle) {
                if (song.equals(newSong)) {
                    song.setPopularity(popularity);
                    updatePop = true;
                }
            }
            // song has same name but no equal
            if (!updatePop) {
                specificTitle.add(newSong);
                this.size++;
            }
        } else {
            ArrayList<PlayableItem> newSpecificTitle = new ArrayList<>();
            newSpecificTitle.add(newSong);
            data.put(name, newSpecificTitle);
            this.size++;
        }

        // adds newSong to artists TreeMap
        if (artists.containsKey(artist)) {
            boolean songDuplicate = false;

            ArrayList<PlayableItem> artistSongs = artists.get(artist);
            for (PlayableItem song : artistSongs) {
                if (song.equals(newSong)) {
                    song.setPopularity(popularity);
                    songDuplicate = true;
                }
            }
            // song does not equal newSong
            if (!songDuplicate) {
                artistSongs.add(newSong);
            }
        } else {
            ArrayList<PlayableItem> newSpecificTitle = new ArrayList<>();
            newSpecificTitle.add(newSong);
            artists.put(artist, newSpecificTitle);
        }
    }

    public ArrayList<PlayableItem> partialSearchBySongName(String name) {
        ArrayList<PlayableItem> specificSongName = new ArrayList<>();
        String nameLower = name.toLowerCase();

        for (String fullName : data.keySet()) {
            String fullNameLower = fullName.toLowerCase();

            if (fullNameLower.contains(nameLower)) {
                specificSongName.addAll(data.get(fullName));
            }
        }

        return specificSongName;
    }

    public ArrayList<PlayableItem> partialSearchByArtistName(String name) {
        ArrayList<PlayableItem> specificArtistName = new ArrayList<>();
        String nameLower = name.toLowerCase();

        for (String fullName : artists.keySet()) {
            String fullNameLower = fullName.toLowerCase();

            if (fullNameLower.contains(nameLower)) {
                specificArtistName.addAll(artists.get(fullName));
            }
        }

        // sorting popularity from highest to lowest
        specificArtistName.sort((song1, song2) -> song2.getPopularity() - song1.getPopularity());
        return specificArtistName;
    }

    public ArrayList<PlayableItem> searchHighestPopularity(int threshold) {
        ArrayList<PlayableItem> specificPopularity = new ArrayList<>();

        for (String songName : data.keySet()) {
            ArrayList<PlayableItem> songNameList = data.get(songName);
            for (PlayableItem song : songNameList) {
                if (song.getPopularity() >= threshold) {
                    specificPopularity.add(song);
                }
            }
        }

        // sorting popularity from highest to lowest
        specificPopularity.sort((song1, song2) -> song2.getPopularity() - song1.getPopularity());
        return specificPopularity;
    }

    public ArrayList<PlayableItem> getRecommendedSongs(List<String> fiveArtists) {
        recommender = new Recommendation("UserData.csv");
        String[] recommendedArtists = recommender.recommendNewArtists(fiveArtists);

        ArrayList<PlayableItem> totalSongs = new ArrayList<>();
        // making arraylist of total valid songs (PlayableItems);
        for (String artist : recommendedArtists) {
            if (artists.containsKey(artist)) {
                totalSongs.addAll(artists.get(artist));
            }
        }

        ArrayList<PlayableItem> recommendedSongs = new ArrayList<>();

        if (totalSongs.size() <= 10) {
            recommendedSongs = totalSongs;
        } else {
            // sorting by popularity in descending order
            totalSongs.sort((song1, song2) -> song2.getPopularity() - song1.getPopularity());

            // filtering top 10 songs
            for (int i = 0; i < 10; i++) {
                recommendedSongs.add(totalSongs.get(i));
            }
        }

        return recommendedSongs;
    }

    public int size() {
        return this.size;
    }
}

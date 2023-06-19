import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Set;


public class Recommendation {

    Map<Long, HashMap<String, Integer>> userData;

    public Recommendation(String filePath) {
        userData = new HashMap<>();
        parseCsvFile(filePath);
    }

    private void parseCsvFile(String csvFilePath) {
        List<List<String>> rawUsersList = new ArrayList<>();

        // reading csvFilePath
        try {
            FileReader userFr = new FileReader(csvFilePath);
            BufferedReader userBr = new BufferedReader(userFr);
            String line;

            userBr.readLine();
            while ((line = userBr.readLine()) != null) {
                String[] values = line.split(",");
                rawUsersList.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            return;
        }

        for (List<String> songAttributes : rawUsersList) {
            // format: User ID,Artist Name,Song Name,Minutes Listened
            Long userID = Long.parseLong(songAttributes.get(0)); // key for outer Hashtable
            String artistName = songAttributes.get(1); // key for inner Hashtable
            String songName = songAttributes.get(2);
            int minutesListened = Integer.parseInt(songAttributes.get(3)); // value is sum of this
            // adding to userData HashTable
            if (userData.containsKey(userID)) {
                // gets inner Hashtable
                HashMap<String, Integer> songsTable = userData.get(userID);
                // checks if artist is in inner hashtable
                if (songsTable.containsKey(artistName)) {
                    songsTable.put(artistName, songsTable.get(artistName) + minutesListened);
                } else {
                    // creates new inner key and value pair
                    songsTable.put(artistName, minutesListened);
                }
            } else {
                // creates inner HashTable
                HashMap<String, Integer> newSongsTable = new HashMap<>();
                newSongsTable.put(artistName, minutesListened);
                // put new inner HashTable into userData
                userData.put(userID, newSongsTable);
            }
        }

        // sorts the userData by total minutes listened in descending order
        Map<Long, HashMap<String, Integer>> sortedUserData = new HashMap<>();
        for (Long userID : userData.keySet()) {
            HashMap<String, Integer> unsortedSongs = userData.get(userID);
            // Creating a "sorted" list from HashTable values
            List<Map.Entry<String, Integer>> sortedList =
                    new LinkedList<Map.Entry<String, Integer>>(unsortedSongs.entrySet());
            // Sort in descending order
            sortedList.sort((o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));
            // Creating new LinkedHashMap to store sorted songs in descending order
            LinkedHashMap<String, Integer> sortedMinutes = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> entry : sortedList) {
                sortedMinutes.put(entry.getKey(), entry.getValue());
            }
            // put sorted map for each userID
            sortedUserData.put(userID, sortedMinutes);
        }

        // filters top 5 artists from the sorted hashTable
        Map<Long, HashMap<String, Integer>> filteredUserData = new HashMap<>();
        for (long userID : sortedUserData.keySet()) {
            HashMap<String, Integer> sortedSongs = sortedUserData.get(userID);
            // using iterator to get the first 5 artists with the most listens
            int top5counter = 0;
            LinkedHashMap<String, Integer> filteredSongs = new LinkedHashMap<>();
            for (String key : sortedSongs.keySet()) {
                filteredSongs.put(key, sortedSongs.get(key));
                top5counter++;
                if (top5counter == 5) {
                    break;
                }
            }
            filteredUserData.put(userID, filteredSongs);
        }
        userData = filteredUserData;
    }

    public String[] recommendNewArtists(List<String> artistList) {
        // creating hashmap with keys as userID and values as jaccard similarity scores
        HashMap<Long, Double> jaccardUsers = new HashMap<>();

        for (Long userID : userData.keySet()) {
            HashMap<String, Integer> sortedSongs = userData.get(userID);
            List<String> otherArtistList = new ArrayList<>(sortedSongs.keySet());

            double jaccardValue = jaccardSimilarityScore(artistList, otherArtistList);

            jaccardUsers.put(userID, jaccardValue);
        }

        // first, sort jaccard scores in descending order
        // Creating a "sorted" list from HashTable values
        List<Map.Entry<Long, Double>> sortedJaccardList =
                new LinkedList<Map.Entry<Long, Double>>(jaccardUsers.entrySet());

        // Sort in descending order
        sortedJaccardList.sort((o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));

        // Creating new LinkedHashMap to store sorted songs in descending order
        LinkedHashMap<Long, Double> sortedJaccardUsers = new LinkedHashMap<>();
        for (Map.Entry<Long, Double> entry : sortedJaccardList) {
            sortedJaccardUsers.put(entry.getKey(), entry.getValue());
        }

        // next, filter top 3 users from sorted scores
        List<Long> top3Users = new ArrayList<>();
        int top3counter = 0;
        for (Long userID : sortedJaccardUsers.keySet()) {
            top3Users.add(userID);
            top3counter++;
            if (top3counter == 3) {
                break;
            }
        }

        // final step, getting a String[] of new artist names from the top 3 users
        // new = remove the artist names that are equal to names given in artistList
        // first, make arraylist of all unique new artists
        List<String> newArtists = new ArrayList<>();
        for (Long userID : top3Users) {
            HashMap<String, Integer> artistsTable = userData.get(userID);

            for (String artist : artistsTable.keySet()) {
                if (!artistList.contains(artist)) {
                    newArtists.add(artist);
                }
            }
        }

        // second, make sure arraylist has all unique values
        // passing into hash set makes the values unique
        HashSet<String> artistSet = new HashSet<String>(newArtists);
        // convert back to arraylist
        ArrayList<String> uniqueArtists = new ArrayList<>(artistSet);

        // finally, change arrayList into String[]
        String[] recommendedArtists = new String[uniqueArtists.size()];
        for (int i = 0; i < uniqueArtists.size(); i++) {
            recommendedArtists[i] = uniqueArtists.get(i);
        }

        return recommendedArtists;
    }

    private double jaccardSimilarityScore(List<String> user1Artists, List<String> user2Artists) {
        Set<String> user1 = new HashSet<>(user1Artists);
        Set<String> user2 = new HashSet<>(user2Artists);

        int size1 = user1.size();
        int size2 = user2.size();
        user1.retainAll(user2);
        int intersection = user1.size();
        return 1d / (size1 + size2 - intersection) * intersection;
    }

}

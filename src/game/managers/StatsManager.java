/**
 * SUMMARY: Handles saving and loading the player's progression and high scores.
 * Reads and writes data to basic text files in the data folder.
 */

package game.managers;

import game.core.Game;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StatsManager {
    private static String DIR = "data/";

    /** Points saving and loading at a different folder. Used by the unit tests so they never touch real save data. */
    public static void setDataDir(String dir) {
        DIR = dir.endsWith("/") ? dir : dir + "/";
    }

    private static String getFileName(int levelNum) {
        return DIR + "stats_lvl" + levelNum + ".txt";
    }

    public static void saveProgression(int highestLevel) {
        try {
            File dir = new File(DIR);
            if (!dir.exists()) dir.mkdirs();

            int currentHighest = loadProgression();
            if (highestLevel > currentHighest) {
                // [Java Topic: File I/O] BufferedWriter makes saving files a bit faster and safer
                FileWriter fw = new FileWriter(DIR + "progression.txt", false);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(String.valueOf(highestLevel));
                bw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int loadProgression() {
        try {
            File f = new File(DIR + "progression.txt");
            if (!f.exists()) return 1;

            // [Java Topic: File I/O] BufferedReader reads text lines efficiently
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = br.readLine();
            br.close();
            if (line != null) {
                // String to int conversion
                return Integer.parseInt(line.trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static void addScore(int levelNum, int score) {
        try {
            File dir = new File(DIR);
            if (!dir.exists()) dir.mkdirs();

            // the "true" flag means we append to the file instead of overwriting it
            FileWriter fw = new FileWriter(getFileName(levelNum), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(score + "\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // [Java Topic: Collections] Returns a List so UI can draw the graph easily
    public static List<Integer> getRecentScores(int levelNum) {
        List<Integer> scores = new ArrayList<>();
        try {
            File f = new File(getFileName(levelNum));
            if (!f.exists()) return scores;

            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    scores.add(Integer.parseInt(line.trim()));
                } catch (NumberFormatException ignored) {}
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Just keep the last 10 scores so the graph doesn't get squished
        if (scores.size() > 10) {
            return scores.subList(scores.size() - 10, scores.size());
        }
        return scores;
    }

    public static void clearStats() {
        for (int i = 1; i <= 3; i++) {
            File f = new File(getFileName(i));
            if (f.exists()) f.delete();
        }

        File progFile = new File(DIR + "progression.txt");
        if (progFile.exists()) progFile.delete();

        // Lock everything back up!
        Game.level2Unlocked = false;
        Game.level3Unlocked = false;
    }
}
/**
 * SUMMARY: Manages the different cosmetic skins available for Targets.
 * It pre-loads the images, tracks which skin the player currently has selected,
 * and saves/loads that preference to a file.
 */

package game.managers;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class TargetManager {

    // [OOP CONCEPT: Nested Data Class] A small helper class to bundle an image, its name, and its file path together.
    public static class TargetData {
        public Image image;
        public String name;
        public String filePath;

        public TargetData(Image image, String name, String filePath) {
            this.image = image;
            this.name = name;
            this.filePath = filePath;
        }
    }

    // [JAVA TOPIC: Collections] A dynamic list holding all our TargetData objects.
    public static ArrayList<TargetData> targets = new ArrayList<>();
    private static int currentIndex = 0;
    private static final String SAVE_FILE = "target_save.txt";

    static {
        Toolkit tk = Toolkit.getDefaultToolkit();

        String defaultPath = "data/targets/target.png";
        targets.add(new TargetData(tk.getImage(defaultPath), "classic_orange", defaultPath));

        String[] customNames = { "t_1", "3d_magic", "3d_blue", "t_2" };

        for (int i = 2; i <= 5; i++) {
            int nameIndex = i - 2;
            // [JAVA TOPIC: Ternary Operator] A shorthand if-else statement.
            // "If we have a custom name, use it. Otherwise, call it 'Target X'".
            String name = (nameIndex < customNames.length) ? customNames[nameIndex] : "Target " + i;
            String path = "data/targets/target_" + i + ".png";

            targets.add(new TargetData(tk.getImage(path), name, path));
        }

        // [JAVA TOPIC: MediaTracker] Java loads images asynchronously (in the background).
        // MediaTracker forces the game to freeze and wait until all images are 100% loaded before continuing.
        MediaTracker tracker = new MediaTracker(new Component() {});
        for(int i = 0; i < targets.size(); i++) {
            tracker.addImage(targets.get(i).image, i);
        }
        try { tracker.waitForAll(); } catch (InterruptedException e) {}

        loadSelection();
    }

    public static Image getCurrentImage() {
        if (currentIndex < 0 || currentIndex >= targets.size()) return targets.get(0).image;
        return targets.get(currentIndex).image;
    }

    public static String getCurrentFilePath() {
        if (currentIndex < 0 || currentIndex >= targets.size()) return targets.get(0).filePath;
        return targets.get(currentIndex).filePath;
    }

    public static TargetData getData(int index) {
        if (index >= 0 && index < targets.size()) return targets.get(index);
        return null;
    }

    public static int getCurrentIndex() { return currentIndex; }

    public static void setIndex(int index) {
        if (index >= 0 && index < targets.size()) {
            currentIndex = index;
            // [Java Topic: Method Calls] Instantly saves the new choice
            saveSelection();
        }
    }

    public static int getCount() { return targets.size(); }

    private static void saveSelection() {
        try (FileWriter fw = new FileWriter(SAVE_FILE)) {
            fw.write(String.valueOf(currentIndex));
        } catch (IOException e) {
            System.out.println("Failed to save target selection.");
        }
    }

    private static void loadSelection() {
        File f = new File(SAVE_FILE);
        if (f.exists()) {
            try (Scanner scanner = new Scanner(f)) {
                if (scanner.hasNextInt()) {
                    int savedIndex = scanner.nextInt();
                    if (savedIndex >= 0 && savedIndex < targets.size()) {
                        currentIndex = savedIndex;
                    }
                }
            } catch (FileNotFoundException e) {}
        }
    }
}
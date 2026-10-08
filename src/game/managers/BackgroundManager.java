/**
 * SUMMARY: Manages the different map background images available to the player.
 * It pre-loads the maps, generates thumbnails for the settings menu,
 * tracks the player's current selection, and saves/loads that preference to a file.
 */

package game.managers;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.ImageIcon;

public class BackgroundManager {

    // [Java Topic: Inner Classes] A mini-class inside a bigger one just to bundle related data together
    public static class BackgroundData {
        public Image image;
        public Image thumbnail;
        public String name;

        public BackgroundData(Image image, String name) {
            this.image = image;
            this.name = name;
        }
    }

    public static ArrayList<BackgroundData> backgrounds = new ArrayList<>();
    private static int currentIndex = 0;
    // [Java Topic: Constants] final means this filename string can never be changed
    private static final String SAVE_FILE = "background_save.txt";

    // [Java Topic: Static Blocks] This runs exactly once when the program first starts up
    static {
        Toolkit tk = Toolkit.getDefaultToolkit();

        backgrounds.add(new BackgroundData(
                tk.getImage("data/maps/background.png"),
                "mountains"
        ));

        // [Java Topic: Arrays] Fixed list of strings for our map names
        String[] customNames = {
                "forest",
                "space",
                "xmas_forest",
                "bricks"
        };

        for (int i = 2; i <= 4; i++) {
            int nameIndex = i - 2;
            String name = (nameIndex < customNames.length) ? customNames[nameIndex] : "Background " + i;
            backgrounds.add(new BackgroundData(
                    tk.getImage("data/maps/background_" + i + ".png"),
                    name
            ));
        }

        MediaTracker tracker = new MediaTracker(new Component() {});
        int id = 0;
        for (BackgroundData bg : backgrounds) {
            tracker.addImage(bg.image, id++);
        }
        try {
            tracker.waitForAll();
        } catch (InterruptedException e) {
            System.out.println("Background loading interrupted");
        }

        for (BackgroundData bg : backgrounds) {
            if (bg.image.getWidth(null) > 0) {
                bg.thumbnail = bg.image.getScaledInstance(100, 75, Image.SCALE_SMOOTH);
                new ImageIcon(bg.thumbnail).getImage();
            } else {
                System.out.println("Warning: Could not load " + bg.name);
            }
        }

        loadSelection();
    }

    public static Image getCurrentImage() {
        if (currentIndex < 0 || currentIndex >= backgrounds.size()) return backgrounds.get(0).image;
        return backgrounds.get(currentIndex).image;
    }

    public static BackgroundData getData(int index) {
        if (index >= 0 && index < backgrounds.size()) return backgrounds.get(index);
        return null;
    }

    public static int getCurrentIndex() { return currentIndex; }

    public static void setIndex(int index) {
        if (index >= 0 && index < backgrounds.size()) {
            currentIndex = index;
            saveSelection();
        }
    }

    public static int getCount() { return backgrounds.size(); }

    // [Java Topic: File I/O] Writing data out to a simple text file so choices remember between sessions
    private static void saveSelection() {
        try (FileWriter fw = new FileWriter(SAVE_FILE)) {
            fw.write(String.valueOf(currentIndex));
        } catch (IOException e) {
            System.out.println("Failed to save background selection.");
        }
    }

    // [Java Topic: File I/O] Reading that same text file when the game boots up
    private static void loadSelection() {
        File f = new File(SAVE_FILE);
        if (f.exists()) {
            try (Scanner scanner = new Scanner(f)) {
                if (scanner.hasNextInt()) {
                    int savedIndex = scanner.nextInt();
                    if (savedIndex >= 0 && savedIndex < backgrounds.size()) {
                        currentIndex = savedIndex;
                    }
                }
            } catch (FileNotFoundException e) {}
        }
    }
}
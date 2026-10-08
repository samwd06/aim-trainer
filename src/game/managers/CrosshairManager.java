/**
 * SUMMARY: Manages the different cosmetic cursor images available for the Crosshair.
 * It pre-loads the images, tracks which crosshair the player currently has selected,
 * and saves/loads that preference to a file.
 */

package game.managers;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class CrosshairManager {

    public static class CrosshairData {
        public Image image;
        public String name;

        public CrosshairData(Image image, String name) {
            this.image = image;
            this.name = name;
        }
    }

    public static ArrayList<CrosshairData> crosshairs = new ArrayList<>();
    private static int currentIndex = 0;
    private static final String SAVE_FILE = "crosshair_save.txt";

    static {
        Toolkit tk = Toolkit.getDefaultToolkit();

        crosshairs.add(new CrosshairData(
                tk.getImage("data/crosshairs/crosshair.png"),
                "default"
        ));

        String[] customNames = {
                "kirby",
                "donut",
                ">.<",
                "dot",
                "green_default",
                "heart_glow",
                "precision",
                "shoot_this_guy",
                "hello_kitty",
                "star_glow"
        };

        // [Java Topic: Standard For Loops] Going through our files from 2 to 11 to load them all
        for (int i = 2; i <= 11; i++) {
            int nameIndex = i - 2;
            String name;
            if (nameIndex < customNames.length) {
                name = customNames[nameIndex];
            } else {
                name = "Crosshair " + i;
            }

            crosshairs.add(new CrosshairData(
                    tk.getImage("data/crosshairs/crosshair_" + i + ".png"),
                    name
            ));
        }

        MediaTracker tracker = new MediaTracker(new Component() {});
        for(int i = 0; i < crosshairs.size(); i++) {
            tracker.addImage(crosshairs.get(i).image, i);
        }
        // [Java Topic: Try-Catch] Ignoring any weird interruptions while images load
        try { tracker.waitForAll(); } catch (InterruptedException e) {}

        loadSelection();
    }

    public static Image getCurrentImage() {
        // Safe check so we don't crash if the index gets messed up somehow
        if (currentIndex < 0 || currentIndex >= crosshairs.size()) return crosshairs.get(0).image;
        return crosshairs.get(currentIndex).image;
    }

    public static CrosshairData getData(int index) {
        if (index >= 0 && index < crosshairs.size()) return crosshairs.get(index);
        return null;
    }

    public static int getCurrentIndex() { return currentIndex; }

    public static void setIndex(int index) {
        if (index >= 0 && index < crosshairs.size()) {
            currentIndex = index;
            saveSelection();
        }
    }

    public static int getCount() { return crosshairs.size(); }

    private static void saveSelection() {
        try (FileWriter fw = new FileWriter(SAVE_FILE)) {
            fw.write(String.valueOf(currentIndex));
        } catch (IOException e) {
            System.out.println("Failed to save crosshair selection: " + e.getMessage());
        }
    }

    private static void loadSelection() {
        File f = new File(SAVE_FILE);
        if (f.exists()) {
            try (Scanner scanner = new Scanner(f)) {
                if (scanner.hasNextInt()) {
                    int savedIndex = scanner.nextInt();
                    if (savedIndex >= 0 && savedIndex < crosshairs.size()) {
                        currentIndex = savedIndex;
                    }
                }
            } catch (FileNotFoundException e) {
            }
        }
    }
}
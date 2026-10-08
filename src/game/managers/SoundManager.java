/**
 * SUMMARY: Loads and controls all the music and sound effects in the game.
 * Uses the built-in SoundClip class from the physics engine.
 */

package game.managers;

import city.cs.engine.SoundClip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class SoundManager {

    // [Java Topic: Arrays] Holding a few copies of the same sound so they can overlap if shot quickly
    private static SoundClip[] hitSounds = new SoundClip[3];
    private static SoundClip[] crackSounds = new SoundClip[3];
    private static SoundClip collectSound;
    private static SoundClip currentMusic;

    private static int hitIndex = 0;
    private static int crackIndex = 0;

    private static float masterVolume = 0.5f;
    private static boolean isMuted = false;

    static {
        // [Java Topic: Try-Catch] Audio loading fails easily if files are missing, so we must catch errors
        try {
            for (int i = 0; i < 3; i++) {
                hitSounds[i] = new SoundClip("data/sounds/hit.wav");
                crackSounds[i] = new SoundClip("data/sounds/crack.wav");
            }
            collectSound = new SoundClip("data/sounds/collect.wav");
            applyVolume();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Error loading sounds: " + e);
        }
    }

    public static void setVolume(float vol) {
        masterVolume = vol;
        applyVolume();
    }

    public static void setMuted(boolean muted) {
        isMuted = muted;
        if (currentMusic != null) {
            if (muted) currentMusic.stop();
            else currentMusic.resume();
        }
    }

    private static void applyVolume() {
        for (SoundClip c : hitSounds) if(c!=null) c.setVolume(masterVolume);
        for (SoundClip c : crackSounds) if(c!=null) c.setVolume(masterVolume);
        if (collectSound != null) collectSound.setVolume(masterVolume);

        if (currentMusic != null) currentMusic.setVolume(masterVolume * 0.4f);
    }

    public static void playMusic(int levelNum) {
        if (currentMusic != null) currentMusic.stop();

        try {
            currentMusic = new SoundClip("data/sounds/music" + levelNum + ".wav");
            currentMusic.setVolume(masterVolume * 0.4f);
            if (!isMuted) currentMusic.loop();
        } catch (Exception e) {
            // [Java Topic: Nested Try-Catch] If the first name fails, let's try an alternative name just in case
            try {
                currentMusic = new SoundClip("data/sounds/level" + levelNum + ".wav");
                currentMusic.setVolume(masterVolume * 0.4f);
                if (!isMuted) currentMusic.loop();
            } catch (Exception ex) {
                System.out.println("No background music found for level " + levelNum + " in the data/sounds/ folder.");
            }
        }
    }

    public static void stopMusic() {
        if (currentMusic != null) currentMusic.stop();
    }

    public static void pauseMusic() {
        if (currentMusic != null) currentMusic.pause();
    }

    public static void resumeMusic() {
        if (currentMusic != null && !isMuted) currentMusic.resume();
    }

    public static void playHit() {
        if (isMuted) return;
        SoundClip c = hitSounds[hitIndex];
        if (c != null) {
            c.stop();
            c.play();
            // Loop the index back to 0 if it hits 3
            hitIndex = (hitIndex + 1) % 3;
        }
    }

    public static void playCrack() {
        if (isMuted) return;
        SoundClip c = crackSounds[crackIndex];
        if (c != null) { c.stop(); c.play(); crackIndex = (crackIndex + 1) % 3; }
    }

    public static void playCollect() {
        if (!isMuted && collectSound != null) collectSound.play();
    }
}
/**
 * SUMMARY: A central dictionary storing the player's control binds.
 * It maps specific keyboard keys and custom mouse codes to in-game actions
 * (like shooting, pausing, and restarting), making them easy to rebind.
 */

package game.input;

import java.awt.event.KeyEvent;

// [OOP CONCEPT: Data/Utility Class] This class just holds global settings for controls.
public class Keybinds {

    // [JAVA TOPIC: Static Variables] These belong to the class itself, so any file can access them via Keybinds.PAUSE
    public static int PAUSE = KeyEvent.VK_ESCAPE;
    public static int RESTART = KeyEvent.VK_F3;

    public static int SHOOT = -101;

    // [OOP CONCEPT: Constants] 'final' means these custom mouse codes can never be altered.
    public static final int MOUSE_LEFT = -101;
    public static final int MOUSE_MIDDLE = -102;
    public static final int MOUSE_RIGHT = -103;
    public static final int MOUSE_SIDE1 = -104;
    public static final int MOUSE_SIDE2 = -105;

    public static String getKeyText(int code) {
        if (code == 0) return "---";

        // [JAVA TOPIC: Switch Statements] A cleaner way to write multiple if/else conditions.
        if (code < -100) {
            switch (code) {
                case MOUSE_LEFT: return "Left Click";
                case MOUSE_MIDDLE: return "Scroll Click";
                case MOUSE_RIGHT: return "Right Click";
                case MOUSE_SIDE1: return "Mouse 4";
                case MOUSE_SIDE2: return "Mouse 5";
                default: return "Mouse " + Math.abs(code + 100);
            }
        }

        try {
            return KeyEvent.getKeyText(code).toUpperCase();
        } catch (Exception e) {
            return "Unknown";
        }
    }
}
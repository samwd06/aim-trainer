/**
 * SUMMARY: Listens to keyboard presses for things like pausing, restarting,
 * or even shooting if the player decides to rebind shoot to a key.
 */
package game.input;

import game.core.Game;
import game.core.GameLevel;
import org.jbox2d.common.Vec2;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

// [Java Topic: Inheritance] KeyAdapter is a built-in Java class that handles keyboard events
public class KeyboardHandler extends KeyAdapter {
    private Game game;
    public static boolean isHoldingShoot = false;

    public KeyboardHandler(Game game) {
        this.game = game;
    }

    // [Java Topic: Method Overriding] We intercept the key press to run our own code
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (Keybinds.PAUSE != 0 && code == Keybinds.PAUSE) {
            game.toggleMenu();
        }

        if (Keybinds.RESTART != 0 && code == Keybinds.RESTART) {
            if (!game.getWorld().isInMainMenu() && !game.getWorld().isPaused()) {
                game.restartGame();
            }
        }

        if (Keybinds.SHOOT != 0 && code == Keybinds.SHOOT) {
            if (!isHoldingShoot) {
                isHoldingShoot = true;
                GameLevel world = game.getWorld();

                // FIXED: Added world.getStartTimer() <= 0 so you can't shoot during countdown
                if (!world.isPaused() && !world.isInMainMenu() && world.getStartTimer() <= 0) {
                    Vec2 aimPos = world.getPlayer().getPosition();
                    world.tryShoot(aimPos);
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (Keybinds.SHOOT != 0 && code == Keybinds.SHOOT) {
            isHoldingShoot = false;
        }
    }
}
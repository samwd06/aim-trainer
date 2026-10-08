/**
 * SUMMARY: Listens to mouse clicks and movements. Updates the cursor position
 * and tells the game to fire a shot when you left click.
 */
package game.input;

import game.core.GameLevel;
import game.bodies.Crosshair;
import city.cs.engine.UserView;
import org.jbox2d.common.Vec2;
import java.awt.event.*;

// [Java Topic: Event Listeners] MouseAdapter gives us empty methods we can override for clicks and drags
public class MouseHandler extends MouseAdapter {
    private UserView view;
    private Crosshair player;
    public static boolean isHoldingShoot = false;

    public MouseHandler(UserView view, Crosshair player) {
        this.view = view;
        this.player = player;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int code = -100 - e.getButton();
        if (code == Keybinds.SHOOT) {
            isHoldingShoot = true;
            GameLevel world = (GameLevel) view.getWorld();

            // FIXED: Added world.getStartTimer() <= 0 so you can't shoot during countdown
            if (!world.isPaused() && !world.isInMainMenu() && world.getStartTimer() <= 0) {
                Vec2 worldPoint = view.viewToWorld(e.getPoint());
                world.tryShoot(worldPoint);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int code = -100 - e.getButton();
        if (code == Keybinds.SHOOT) {
            isHoldingShoot = false;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (player != null) {
            // [Java Topic: Math/Coordinates] Translate flat screen pixels into actual physics engine coordinates
            Vec2 worldPoint = view.viewToWorld(e.getPoint());
            player.setPosition(worldPoint);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }
}
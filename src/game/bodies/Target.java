/**
 * SUMMARY: Represents the stationary targets the player must shoot in Level 1.
 * It hides off-screen and teleports to designated coordinates when active.
 */

package game.bodies;

import game.managers.*;
import city.cs.engine.*;
import org.jbox2d.common.Vec2;

// [OOP CONCEPT: Inheritance] Target inherits from StaticBody.
public class Target extends StaticBody {

    // [OOP CONCEPT: Encapsulation & Constants] 'private static final' means this shape is shared across ALL targets and cannot be changed.
    private static final Shape targetShape = new BoxShape(3.0f, 3.0f);

    // [OOP CONCEPT: Encapsulation] Remembers where this specific target belongs on the grid.
    private Vec2 homePosition;

    public Target(World world, float x, float y) {
        // [JAVA TOPIC: Superclass Constructor] Calls the StaticBody constructor to physically create it in the world.
        super(world, targetShape);

        this.homePosition = new Vec2(x, y);

        updateImage();

        this.hide();
    }

    public void updateImage() {
        this.removeAllImages();
        this.addImage(new BodyImage(TargetManager.getCurrentFilePath(), 6f));
    }

    public void show() {
        this.setPosition(homePosition);
    }

    public void hide() {
        this.setPosition(new Vec2(-1000, -1000));
    }
}
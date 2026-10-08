/**
 * SUMMARY: A physical representation of the player's mouse cursor in the world.
 * It uses a GhostlyFixture, meaning it tracks its physical coordinates but passes harmlessly through other objects without colliding.
 */

package game.bodies;

import city.cs.engine.*;

// [OOP CONCEPT: Inheritance] Extends DynamicBody so it can move around the world freely.
public class Crosshair extends DynamicBody {

    private static final Shape cursorShape = new CircleShape(0.5f);

    public Crosshair(World w) {
        // [JAVA TOPIC: Constructors] We use super(w) WITHOUT passing the shape.
        // This stops the engine from automatically making it a SolidFixture (which would crash into walls).
        super(w);

        // [JAVA TOPIC: Physics Fixtures] We manually create a GhostlyFixture.
        // A GhostlyFixture can detect overlaps, but passes through other bodies without pushing them!
        new GhostlyFixture(this, cursorShape);

        addImage(new BodyImage("data/empty.png", 1f));

        this.setGravityScale(0);
    }
}
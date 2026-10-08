/**
 * SUMMARY: Represents a collectible extra life.
 * It is a green DynamicBody that floats horizontally across the screen for the player to shoot in order to recover lost lives.
 */

package game.bodies;

import game.core.*;
import game.managers.*;
import city.cs.engine.*;

// [OOP CONCEPT: Inheritance & Interfaces] Extends DynamicBody AND implements StepListener methods.
public class Heart extends DynamicBody implements StepListener {

    private static final Shape heartShape = new BoxShape(0.75f, 0.75f);

    public Heart(World world) {
        super(world);

        // [JAVA TOPIC: Physics Fixtures] A SolidFixture gives the body physical mass and collision properties.
        SolidFixture f = new SolidFixture(this, heartShape);
        f.setRestitution(1.0f);
        f.setFriction(0.0f);

        this.setGravityScale(0);

        addImage(new BodyImage("data/ui/heart2.png", 1.5f));

        world.addStepListener(this);
    }

    // [OOP CONCEPT: Polymorphism] Overriding the StepListener method.
    @Override
    public void preStep(StepEvent stepEvent) {
        setAngularVelocity(0);
    }

    @Override
    public void postStep(StepEvent stepEvent) {}

    // [OOP CONCEPT: Polymorphism] Overriding the default destroy method.
    @Override
    public void destroy() {
        // [JAVA TOPIC: Memory Management] Unregister the listener before destroying the body to prevent crashes/memory leaks
        getWorld().removeStepListener(this);
        super.destroy();
    }
}
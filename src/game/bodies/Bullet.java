/**
 * SUMMARY: An invisible, short-lived physical body spawned exactly where the player clicks.
 * It exists for only two frames to allow the physics engine to detect what the player aimed at, before destroying itself.
 */

package game.bodies;

import game.core.*;
import game.managers.*;
import city.cs.engine.*;
import org.jbox2d.common.Vec2;

// [OOP CONCEPT: Multiple Behaviors] Extends DynamicBody and implements StepListener to track its own lifespan.
public class Bullet extends DynamicBody implements StepListener {

    private GameLevel level;
    private boolean hitSomething = false;
    private int framesAlive = 0;

    public Bullet(GameLevel level, Vec2 pos) {
        super(level, new CircleShape(0.2f));
        this.level = level;

        setPosition(pos);
        setGravityScale(0);
        addImage(new BodyImage("data/empty.png", 0.1f));

        level.addStepListener(this);
    }

    public boolean hasHitSomething() { return hitSomething; }
    public void setHitSomething(boolean hit) { this.hitSomething = hit; }

    @Override
    public void preStep(StepEvent stepEvent) {}

    // [OOP CONCEPT: Polymorphism] Overriding StepListener
    @Override
    public void postStep(StepEvent stepEvent) {
        framesAlive++;

        // Keeps bullet alive for exactly 2 frames to ensure the physics engine registers any collisions.
        if (framesAlive >= 2) {

            if (!hitSomething) {
                level.incrementMissed();
                level.decrementLives();
                level.addBulletHole(getPosition());
            }

            level.removeStepListener(this);
            this.destroy();
        }
    }
}
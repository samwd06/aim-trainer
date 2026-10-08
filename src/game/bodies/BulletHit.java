/**
 * SUMMARY: A specialized listener that checks exactly what a bullet hit.
 * Differentiates between hitting targets (gains points) vs hearts (gains lives).
 */
// src/game/bodies/BulletHit.java
package game.bodies;

import game.core.*;
import city.cs.engine.*;

// [Java Topic: Interfaces] Implementing CollisionListener makes this class able to respond to physical bumps
public class BulletHit implements CollisionListener {

    private GameLevel level;
    private Bullet bullet;

    public BulletHit(GameLevel level, Bullet bullet) {
        this.level = level;
        this.bullet = bullet;
    }

    @Override
    public void collide(CollisionEvent e) {
        if (bullet.hasHitSomething()) return;

        // [Java Topic: Type Checking] Making sure we only score points if we hit the right types of objects
        if (e.getOtherBody() instanceof Target ||
                e.getOtherBody().getClass().getSimpleName().equals("MovingTarget") ||
                e.getOtherBody().getClass().getSimpleName().equals("SwitchingTarget")) {

            bullet.setHitSomething(true);
            level.onTargetHit(e.getOtherBody());
        }
        else if (e.getOtherBody() instanceof Heart || e.getOtherBody().getClass().getSimpleName().equals("Level2Heart")) {
            bullet.setHitSomething(true);
            e.getOtherBody().destroy();
            level.clearActiveHeart();
            level.incrementLives();
        }
    }
}
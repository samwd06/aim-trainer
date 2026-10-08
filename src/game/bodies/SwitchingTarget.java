/**
 * SUMMARY: A durable target used in Level 3 that must be held down like a laser
 * to deplete its health before it breaks.
 */
// src/game/bodies/SwitchingTarget.java
package game.bodies;

import game.managers.TargetManager;
import game.core.GameLevel;
import city.cs.engine.*;
import org.jbox2d.common.Vec2;

// [Java Topic: Event Interfaces] Implementing SensorListener lets this act like a trigger pad instead of a solid wall
public class SwitchingTarget extends StaticBody implements SensorListener {
    private static final Shape shape = new CircleShape(1.5f);
    private float health = 100f;
    private boolean isDead = false;

    public SwitchingTarget(World w, float x, float y) {
        super(w);
        this.setPosition(new Vec2(x, y));
        // Sensors detect overlap, but bullets pass right through them
        Sensor sensor = new Sensor(this, shape);
        sensor.addSensorListener(this);
        updateImage();
    }

    public void updateImage() {
        this.removeAllImages();
        this.addImage(new BodyImage(TargetManager.getCurrentFilePath(), 3f));
    }

    public float getHealth() { return health; }

    public void takeDamage(float amount) {
        if (isDead) return;
        health -= amount;
        if (health <= 0) {
            isDead = true;
            ((GameLevel) this.getWorld()).onTargetHit(this);
        }
    }

    @Override
    public void beginContact(SensorEvent e) {
        if (e.getContactBody() instanceof Bullet) {
            Bullet b = (Bullet) e.getContactBody();
            if (!b.hasHitSomething()) {
                b.setHitSomething(true);
                takeDamage(25f);
            }
        }
    }

    @Override public void endContact(SensorEvent e) {}
}
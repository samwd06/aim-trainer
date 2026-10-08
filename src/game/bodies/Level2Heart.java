/**
 * SUMMARY: A special heart variant that uses a Sensor instead of a solid hitbox,
 * preventing bullets from bouncing off it awkwardly in Level 2 and 3.
 */
package game.bodies;

import city.cs.engine.*;
import org.jbox2d.common.Vec2;
import game.core.GameLevel;

public class Level2Heart extends StaticBody implements StepListener, SensorListener {
    private static final Shape shape = new BoxShape(0.75f, 0.75f);
    private int framesAlive = 0;
    private GameLevel level;

    public Level2Heart(GameLevel w, float x, float y) {
        super(w);
        this.level = w;

        // A Sensor detects overlapping bodies (like our bullet) without physically colliding with them!
        Sensor sensor = new Sensor(this, shape);
        sensor.addSensorListener(this);

        this.setPosition(new Vec2(x, y));
        this.addImage(new BodyImage("data/ui/heart2.png", 1.5f));

        w.addStepListener(this);
    }

    @Override
    public void beginContact(SensorEvent e) {
        // [Java Topic: Type Casting] Safely transforming a generic Body back into a Bullet to access its custom methods
        if (e.getContactBody() instanceof Bullet) {
            Bullet b = (Bullet) e.getContactBody();
            if (!b.hasHitSomething()) {
                b.setHitSomething(true);
                level.clearActiveHeart();
                level.incrementLives();
                this.destroy();
            }
        }
    }

    @Override
    public void endContact(SensorEvent e) {}

    @Override
    public void preStep(StepEvent stepEvent) {}

    @Override
    public void postStep(StepEvent stepEvent) {
        framesAlive++;
        if (framesAlive >= 120) {
            level.clearActiveHeart();
            this.destroy();
        }
    }

    @Override
    public void destroy() {
        level.removeStepListener(this);
        super.destroy();
    }
}
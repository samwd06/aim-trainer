/**
 * SUMMARY: A dynamic target that moves around the arena and bounces off walls.
 * Used heavily in Level 2.
 */

package game.bodies;

import game.managers.TargetManager;
import city.cs.engine.*;
import org.jbox2d.common.Vec2;
import java.util.Random;

// Extends DynamicBody so physics affect it, and StepListener to maintain constant speed.
public class MovingTarget extends DynamicBody implements StepListener {

    private static final Shape shape = new CircleShape(1.5f);
    private float speed;

    public MovingTarget(World w, float x, float y, float speed) {
        super(w, shape);
        this.speed = speed;
        this.setPosition(new Vec2(x, y));

        this.setGravityScale(0);
        SolidFixture fixture = new SolidFixture(this, shape);
        fixture.setRestitution(1.0f); // Perfect bounce
        fixture.setFriction(0.0f);    // No slowing down when sliding

        // [Java Topic: Math Class] Using trigonometry to pick a random launch direction
        Random rand = new Random();
        float angle = rand.nextFloat() * (float)Math.PI * 2;
        this.setLinearVelocity(new Vec2((float)Math.cos(angle) * speed, (float)Math.sin(angle) * speed));

        updateImage();
        w.addStepListener(this);
    }

    public void updateImage() {
        this.removeAllImages();
        this.addImage(new BodyImage(TargetManager.getCurrentFilePath(), 3f));
    }

    @Override
    public void preStep(StepEvent e) {}

    @Override
    public void postStep(StepEvent e) {
        // [Java Topic: Vector Math] Force the target to maintain its exact speed even after bouncing
        Vec2 v = this.getLinearVelocity();
        float currentSpeed = v.length();
        if (currentSpeed > 0) {
            this.setLinearVelocity(new Vec2((v.x / currentSpeed) * speed, (v.y / currentSpeed) * speed));
        }
    }

    @Override
    public void destroy() {
        this.getWorld().removeStepListener(this);
        super.destroy();
    }
}
/**
 * SUMMARY: The second level. Here, targets move smoothly around the screen
 * and bounce perfectly off the walls without slowing down.
 */
package game.core;

import game.bodies.*;
import city.cs.engine.*;
import org.jbox2d.common.Vec2;
import java.util.ArrayList;
import java.util.Random;

public class Level2 extends GameLevel {

    private Crosshair player;
    private ArrayList<MovingTarget> activeTargets;
    private float targetSpeed = 8f;

    public Level2() {
        super();
        activeTargets = new ArrayList<>();

        Shape horizWall = new BoxShape(25, 0.5f);
        Shape vertWall = new BoxShape(0.5f, 20);

        StaticBody topBody = new StaticBody(this, horizWall); topBody.addImage(new BodyImage("data/empty.png", 1f));
        StaticBody botBody = new StaticBody(this, horizWall); botBody.addImage(new BodyImage("data/empty.png", 1f));
        StaticBody leftBody = new StaticBody(this, vertWall); leftBody.addImage(new BodyImage("data/empty.png", 1f));
        StaticBody rightBody = new StaticBody(this, vertWall); rightBody.addImage(new BodyImage("data/empty.png", 1f));

        SolidFixture tWall = new SolidFixture(topBody, horizWall); tWall.setRestitution(1f);
        SolidFixture bWall = new SolidFixture(botBody, horizWall); bWall.setRestitution(1f);
        SolidFixture lWall = new SolidFixture(leftBody, vertWall); lWall.setRestitution(1f);
        SolidFixture rWall = new SolidFixture(rightBody, vertWall); rWall.setRestitution(1f);

        topBody.setPosition(new Vec2(0, 11.5f));
        botBody.setPosition(new Vec2(0, -15.5f));
        leftBody.setPosition(new Vec2(-20.5f, 0));
        rightBody.setPosition(new Vec2(20.5f, 0));

        player = new Crosshair(this);
        player.setPosition(new Vec2(0, 0));

        this.addStepListener(new GameTimer(this));
    }

    // [Java Topic: Method Overriding] We swap the base method out for our custom bouncy bullets logic
    @Override
    public void tryShoot(Vec2 targetPos) {
        Bullet b = new Bullet(this, targetPos);
        b.addCollisionListener(new BulletHit(this, b));
    }

    @Override
    public void startLevel() {
        for (int i = 0; i < 3; i++) spawnNewTarget();
    }

    @Override
    public void resetLevelState() {
        for (MovingTarget t : activeTargets) t.destroy();
        activeTargets.clear();
        resetHeartState();
    }

    @Override
    public void restartLevel() {
        super.resetStats();
        resetLevelState();
    }

    @Override
    public void hideTargets() {
        for (MovingTarget t : activeTargets) t.removeAllImages();
    }

    @Override
    public void restoreTargets() {
        refreshTargets();
    }

    @Override
    public void refreshTargets() {
        for (MovingTarget t : activeTargets) t.updateImage();
    }

    @Override
    public void onTargetHit(city.cs.engine.Body target) {
        playHitSound();
        incrementScore();
        activeTargets.remove(target);
        target.destroy();
        spawnNewTarget();
    }

    public void spawnNewTarget() {
        float x = (rand.nextFloat() * 30) - 15;
        float y = (rand.nextFloat() * 18) - 10;
        MovingTarget t = new MovingTarget(this, x, y, targetSpeed);
        activeTargets.add(t);
    }

    @Override
    protected void spawnHeart() {
        if (heartsSpawnedCount >= MAX_HEARTS || activeHeart != null) return;

        float x = (rand.nextFloat() * 30) - 15;
        float y = (rand.nextFloat() * 18) - 10;

        activeHeart = new Level2Heart(this, x, y);
        heartsSpawnedCount++;
    }

    @Override
    public Crosshair getPlayer() { return player; }
}
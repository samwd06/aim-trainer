/**
 * SUMMARY: The final level. Spawns "SwitchingTargets" which need to be held down
 * and destroyed over time like a laser tracking scenario.
 */
// src/game/core/Level3.java
package game.core;

import game.bodies.*;
import game.input.MouseHandler;
import game.input.KeyboardHandler;
import city.cs.engine.*;
import org.jbox2d.common.Vec2;
import java.util.ArrayList;

// [Java Topic: Multiple Interfaces] We extend the base level and also implement StepListener for frame-by-frame updates
public class Level3 extends GameLevel implements StepListener {

    private Crosshair player;
    // [Java Topic: Generics] Specifying exactly what type of object this ArrayList holds
    private ArrayList<SwitchingTarget> activeTargets;

    public Level3() {
        super();
        activeTargets = new ArrayList<>();

        Shape horizWall = new BoxShape(25, 0.5f);
        Shape vertWall = new BoxShape(0.5f, 20);

        StaticBody topBody = new StaticBody(this, horizWall); topBody.addImage(new BodyImage("data/empty.png", 1f));
        StaticBody botBody = new StaticBody(this, horizWall); botBody.addImage(new BodyImage("data/empty.png", 1f));
        StaticBody leftBody = new StaticBody(this, vertWall); leftBody.addImage(new BodyImage("data/empty.png", 1f));
        StaticBody rightBody = new StaticBody(this, vertWall); rightBody.addImage(new BodyImage("data/empty.png", 1f));

        topBody.setPosition(new Vec2(0, 11.5f));
        botBody.setPosition(new Vec2(0, -15.5f));
        leftBody.setPosition(new Vec2(-20.5f, 0));
        rightBody.setPosition(new Vec2(20.5f, 0));

        player = new Crosshair(this);
        player.setPosition(new Vec2(0, 0));

        this.addStepListener(new GameTimer(this));
        this.addStepListener(this);
    }

    public ArrayList<SwitchingTarget> getActiveTargets() { return activeTargets; }

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
        for (SwitchingTarget t : activeTargets) t.destroy();
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
        for (SwitchingTarget t : activeTargets) t.removeAllImages();
    }

    @Override
    public void restoreTargets() {
        refreshTargets();
    }

    @Override
    public void refreshTargets() {
        for (SwitchingTarget t : activeTargets) t.updateImage();
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
        SwitchingTarget t = new SwitchingTarget(this, x, y);
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

    @Override
    public void preStep(StepEvent stepEvent) {
        if (isPaused() || isInMainMenu() || getStartTimer() > 0 || isGameOver()) return;

        // "Laser" damage logic: deal small damage every single frame while the mouse is held down
        if (MouseHandler.isHoldingShoot || KeyboardHandler.isHoldingShoot) {
            Vec2 aim = player.getPosition();
            for (int i = activeTargets.size() - 1; i >= 0; i--) {
                SwitchingTarget t = activeTargets.get(i);
                float dist = t.getPosition().sub(aim).length();
                if (dist <= 1.5f) {
                    t.takeDamage(100f / 60f);
                }
            }
        }
    }

    @Override
    public void postStep(StepEvent stepEvent) {}
}
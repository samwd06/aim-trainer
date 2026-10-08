/**
 * SUMMARY: The simplest level. Spawns stationary targets in a grid
 * pattern one by one.
 */
package game.core;

import game.bodies.*;
import city.cs.engine.*;
import org.jbox2d.common.Vec2;
import java.util.ArrayList;
import java.util.Random;

// [JAVA TOPIC: Inheritance]
// By writing "extends GameLevel", Level1 automatically gets all the score, health,
// and timer logic we already wrote. We just have to fill in the specific details for this map.
public class Level1 extends GameLevel {

    private Crosshair player;
    private ArrayList<Target> targets;
    private Random rand;
    private Target currentTarget;

    public Level1() {
        // 'super()' calls the constructor of GameLevel first to set up the physics engine
        super();

        targets = new ArrayList<>();
        rand = new Random();

        Shape horizWall = new BoxShape(25, 0.5f);
        Shape vertWall = new BoxShape(0.5f, 20);

        new StaticBody(this, horizWall).setPosition(new Vec2(0, -15.5f));
        new StaticBody(this, horizWall).setPosition(new Vec2(0, 15.5f));
        new StaticBody(this, vertWall).setPosition(new Vec2(-20.5f, 0));
        new StaticBody(this, vertWall).setPosition(new Vec2(20.5f, 0));

        player = new Crosshair(this);
        player.setPosition(new Vec2(0, 0));

        // [JAVA TOPIC: Nested Loops]
        // This is a classic way to build a 2D grid.
        // The outer loop handles the columns (x), and the inner loop handles the rows (y).
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                float xPos = (i * 8) - 8;
                float yPos = (j * 8) - 10;

                Target t = new Target(this, xPos, yPos);
                t.hide();
                targets.add(t);
            }
        }

        this.addStepListener(new GameTimer(this));
    }

    // [JAVA TOPIC: Method Overriding]
    // The @Override tag tells Java we are providing the specific implementation
    // for an abstract method we inherited from GameLevel.
    @Override
    public void tryShoot(Vec2 targetPos) {
        Bullet b = new Bullet(this, targetPos);
        b.addCollisionListener(new BulletHit(this, b));
    }

    @Override public void startLevel() { spawnRandomTarget(); }
    @Override public void hideTargets() { hideCurrentTarget(); }
    @Override public void restoreTargets() { restoreCurrentTarget(); }

    // [JAVA TOPIC: Randomization]
    public void spawnRandomTarget() {
        hideAllTargets();
        // Pick a random number between 0 and the size of our target list (usually 9)
        int index = rand.nextInt(targets.size());

        currentTarget = targets.get(index);
        currentTarget.show();
    }

    // [JAVA TOPIC: Enhanced For-Loop (For-Each)]
    // A much cleaner way to iterate through every item in an ArrayList.
    // Reads as: "For each Target 't' inside the 'targets' list..."
    public void hideAllTargets() {
        for (Target t : targets) t.hide();
    }

    // [JAVA TOPIC: Null Checks]
    // Always check if an object actually exists before calling methods on it,
    // otherwise the game will crash with a NullPointerException!
    public void hideCurrentTarget() {
        if (currentTarget != null) currentTarget.hide();
    }

    public void restoreCurrentTarget() {
        if (currentTarget != null) currentTarget.show();
    }

    @Override
    public void resetLevelState() {
        hideAllTargets();
        currentTarget = null;
        resetHeartState();
    }

    @Override
    public void restartLevel() {
        // 'super.resetStats()' specifically triggers the reset method in the parent class
        super.resetStats();
        resetLevelState();
    }

    @Override
    public void refreshTargets() {
        for (Target t : targets) t.updateImage();
    }

    @Override
    public void onTargetHit(city.cs.engine.Body target) {
        playHitSound();
        incrementScore();
        spawnRandomTarget();
    }

    @Override
    public Crosshair getPlayer() { return player; }
}
/**
 * SUMMARY: The core blueprint for every level. It holds the score, lives, and timer.
 * Level 1, 2, and 3 all build off of this foundation.
 */
package game.core;

import game.bodies.*;
import game.managers.*;
import city.cs.engine.World;
import org.jbox2d.common.Vec2;
import java.util.ArrayList;

// [JAVA TOPIC: Abstract Classes & Inheritance]
// Making this 'abstract' means we can never actually play a raw "GameLevel".
// It only exists to act as a blueprint that Level1, Level2, and Level3 will 'extend' (inherit from).
public abstract class GameLevel extends World {

    // [JAVA TOPIC: Encapsulation]
    // Keeping these private so external classes can't randomly change the score or lives
    // without using the proper methods below.
    private boolean inMainMenu = true;
    private boolean isPaused = false;
    private boolean scoreRecorded = false;

    private int score = 0;
    private int missed = 0;
    private int lives = 10;
    private int timeRemaining = 60;
    private int startTimer = 3;

    // [JAVA TOPIC: ArrayLists]
    // A dynamic list that shrinks and grows as bullet holes appear and fade away.
    private ArrayList<BulletHole> bulletHoles = new ArrayList<>();

    // [JAVA TOPIC: Access Modifiers]
    // 'protected' is the sweet spot between private and public. It means ONLY the child classes
    // (like Level2 and Level3) are allowed to touch these variables directly.
    protected city.cs.engine.Body activeHeart;
    protected int heartsSpawnedCount = 0;
    protected final int MAX_HEARTS = 5;
    protected int lastSpawnTime = -1;

    protected final float[] safeYLevels = { -6f, 2f, -14f };
    protected java.util.Random rand = new java.util.Random();

    public GameLevel() { super(); }

    public int getAccuracy() {
        int totalShots = score + missed;
        if (totalShots == 0) return 100;

        // [JAVA TOPIC: Casting] Force the math to use floats so we don't lose the decimal places before multiplying by 100.
        return (int) ((score / (float) totalShots) * 100);
    }

    public boolean isInMainMenu() { return inMainMenu; }
    public void setInMainMenu(boolean b) { this.inMainMenu = b; }

    public boolean isScoreRecorded() { return scoreRecorded; }
    public void setScoreRecorded(boolean val) { this.scoreRecorded = val; }

    public void resetStats() {
        score = 0;
        missed = 0;
        lives = 10;
        timeRemaining = 60;
        startTimer = 3;
        bulletHoles.clear();
        isPaused = false;
        inMainMenu = false;
        scoreRecorded = false;

        resetHeartState();
        this.start();
    }

    public void setMasterVolume(float volume) { SoundManager.setVolume(volume); }
    public void setMuted(boolean muted) { SoundManager.setMuted(muted); }
    public void playHitSound() { SoundManager.playHit(); }
    public void playCrackSound() { SoundManager.playCrack(); }

    public boolean isPaused() { return isPaused; }
    public void setPaused(boolean paused) {
        this.isPaused = paused;
        if (isPaused) this.stop(); else this.start();
    }

    public int getLives() { return lives; }

    public void decrementLives() {
        lives--;
        if (lives < 0) lives = 0;
        playCrackSound();
    }

    public void incrementLives() {
        lives++;
        SoundManager.playCollect();
    }

    public boolean isGameOver() { return timeRemaining <= 0 || lives <= 0; }

    public void addBulletHole(Vec2 pos) { bulletHoles.add(new BulletHole(pos)); }

    public void updateBulletHoles() {
        // Counting backwards through the list. If we count forwards and delete something,
        // the list shifts down and we end up skipping the next item!
        for (int i = bulletHoles.size() - 1; i >= 0; i--) {
            BulletHole b = bulletHoles.get(i);
            b.ageUp();
            if (b.getAge() > 3.0f) bulletHoles.remove(i);
        }
    }

    public ArrayList<BulletHole> getBulletHoles() { return bulletHoles; }

    public int getScore() { return score; }
    public void incrementScore() { score++; }
    public int getMissed() { return missed; }
    public void incrementMissed() { missed++; }
    public int getTime() { return timeRemaining; }
    public void decrementTime() { timeRemaining--; }
    public int getStartTimer() { return startTimer; }
    public void decrementStartTimer() { startTimer--; }

    public void updateHeartLifecycle() {
        int t = getTime();
        // The modulo operator (%) checks if the time is perfectly divisible by 10.
        if (t < 60 && t > 0 && t % 10 == 0) {
            if (t != lastSpawnTime) {
                spawnHeart();
                lastSpawnTime = t;
            }
        }
    }

    protected void spawnHeart() {
        if (heartsSpawnedCount >= MAX_HEARTS || activeHeart != null) return;

        int laneIndex = rand.nextInt(safeYLevels.length);
        float y = safeYLevels[laneIndex];

        Heart h = new Heart(this);
        h.setPosition(new Vec2(-18, y));
        h.setLinearVelocity(new Vec2(7, 0));

        activeHeart = h;
        heartsSpawnedCount++;
    }

    public void clearActiveHeart() { this.activeHeart = null; }

    public void removeHeart() {
        if (activeHeart != null) {
            activeHeart.destroy();
            activeHeart = null;
        }
    }

    public void resetHeartState() {
        removeHeart();
        heartsSpawnedCount = 0;
        lastSpawnTime = -1;
    }

    // [JAVA TOPIC: Abstract Methods]
    // These methods have no body (no curly braces).
    // This is basically a contract that forces Level1, Level2, and Level3 to write their own specific versions of these actions.
    public abstract Crosshair getPlayer();
    public abstract void onTargetHit(city.cs.engine.Body target);
    public abstract void tryShoot(Vec2 targetPos);
    public abstract void startLevel();
    public abstract void restartLevel();
    public abstract void resetLevelState();
    public abstract void hideTargets();
    public abstract void restoreTargets();
    public abstract void refreshTargets();
}
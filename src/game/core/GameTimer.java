/**
 * SUMMARY: Acts as the main clock for the game. Runs every single frame to age up bullet holes,
 * count down the timer, and check if the game is over.
 */
package game.core;

import game.managers.*;
import city.cs.engine.*;

// [Java Topic: Interfaces] Implementing StepListener means this class MUST have preStep and postStep methods
public class GameTimer implements StepListener {

    private GameLevel level;
    private int frameCount = 0;

    public GameTimer(GameLevel level) {
        this.level = level;
    }

    @Override
    public void preStep(StepEvent stepEvent) {
        // Skip updating time if we are in a menu
        if (level.isPaused() || level.isInMainMenu()) return;

        level.updateBulletHoles();

        if (level.isGameOver()) {
            if (!level.isScoreRecorded()) {
                int lvlNum = 1;
                // [Java Topic: String comparison] Finding out what class type the level is using its name
                if (level.getClass().getSimpleName().equals("Level2")) lvlNum = 2;
                if (level.getClass().getSimpleName().equals("Level3")) lvlNum = 3;

                StatsManager.addScore(lvlNum, level.getScore());
                level.setScoreRecorded(true);

                // [Java Topic: Instanceof] Another way to check class types
                if (level instanceof Level1 && level.getTime() <= 0) {
                    Game.level2Unlocked = true;
                    StatsManager.saveProgression(2);
                    if (Game.instance != null) Game.instance.onLevelCompleted();
                }
                if (level.getClass().getSimpleName().equals("Level2") && level.getTime() <= 0) {
                    Game.level3Unlocked = true;
                    StatsManager.saveProgression(3);
                    if (Game.instance != null) Game.instance.onLevelCompleted();
                }
            }
            level.stop(); // Stop the physics engine completely
            return;
        }

        if (level.getStartTimer() <= 0) {
            level.updateHeartLifecycle();
        }

        frameCount++;

        // Because the game runs at 60 fps, 60 frames equals 1 real-life second
        if (frameCount >= 60) {
            if (level.getStartTimer() > 0) {
                level.decrementStartTimer();
                if (level.getStartTimer() == 0) level.startLevel();
            } else {
                level.decrementTime();
            }
            frameCount = 0; // Reset frame counter for the next second
        }
    }

    @Override
    public void postStep(StepEvent stepEvent) {}
}
package game.managers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for saving and loading scores and level progression.
 * Every test runs against a temporary folder, so real save data is never touched.
 */
class StatsManagerTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void useTemporaryFolder() {
        StatsManager.setDataDir(tempDir.toString());
    }

    @AfterEach
    void restoreRealFolder() {
        StatsManager.setDataDir("data/");
    }

    @Test
    void noSavedScoresGivesEmptyList() {
        assertTrue(StatsManager.getRecentScores(1).isEmpty());
    }

    @Test
    void savedScoresAreLoadedInOrder() {
        StatsManager.addScore(1, 12);
        StatsManager.addScore(1, 30);
        StatsManager.addScore(1, 7);

        assertEquals(List.of(12, 30, 7), StatsManager.getRecentScores(1));
    }

    @Test
    void eachLevelKeepsItsOwnScores() {
        StatsManager.addScore(1, 10);
        StatsManager.addScore(2, 99);

        assertEquals(List.of(10), StatsManager.getRecentScores(1));
        assertEquals(List.of(99), StatsManager.getRecentScores(2));
    }

    @Test
    void onlyTheLastTenScoresAreReturned() {
        for (int score = 1; score <= 15; score++) {
            StatsManager.addScore(1, score);
        }

        List<Integer> recent = StatsManager.getRecentScores(1);

        assertEquals(10, recent.size());
        assertEquals(6, recent.get(0));
        assertEquals(15, recent.get(9));
    }

    @Test
    void corruptedLinesInTheSaveFileAreSkipped() throws IOException {
        Files.writeString(tempDir.resolve("stats_lvl1.txt"), "20\nnot a number\n\n 35 \n");

        assertEquals(List.of(20, 35), StatsManager.getRecentScores(1));
    }

    @Test
    void progressionStartsAtLevelOne() {
        assertEquals(1, StatsManager.loadProgression());
    }

    @Test
    void progressionIsSavedAndLoaded() {
        StatsManager.saveProgression(2);

        assertEquals(2, StatsManager.loadProgression());
    }

    @Test
    void progressionNeverGoesBackwards() {
        StatsManager.saveProgression(3);
        StatsManager.saveProgression(2);

        assertEquals(3, StatsManager.loadProgression());
    }

    @Test
    void corruptedProgressionFileFallsBackToLevelOne() throws IOException {
        Files.writeString(tempDir.resolve("progression.txt"), "three");

        assertEquals(1, StatsManager.loadProgression());
    }

    @Test
    void clearStatsRemovesScoresAndProgression() {
        StatsManager.addScore(1, 50);
        StatsManager.addScore(3, 80);
        StatsManager.saveProgression(3);

        StatsManager.clearStats();

        assertTrue(StatsManager.getRecentScores(1).isEmpty());
        assertTrue(StatsManager.getRecentScores(3).isEmpty());
        assertEquals(1, StatsManager.loadProgression());
    }
}

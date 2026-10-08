/**
 * SUMMARY: The heart of the program. Sets up the main window, manages
 * switching between levels, and draws the Heads Up Display (HUD) on top of the physics world.
 */
package game.core;

import city.cs.engine.*;
import game.bodies.*;
import game.managers.*;
import game.ui.*;
import game.input.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.AlphaComposite;
import java.awt.image.BufferedImage;
import java.awt.font.TextLayout;
import java.awt.font.FontRenderContext;
import java.awt.Shape;

public class Game {

    // [Java Topic: Static Variables] A static instance allows other classes to easily reference the main game
    public static Game instance;

    // [Java Topic: Polymorphism] The world variable is declared as the parent class GameLevel,
    // but it will hold specific child objects like Level1, Level2, or Level3.
    private GameLevel world;
    private final UserView view;
    private final GameOverlay overlay;

    private MouseHandler mouseController;

    public static boolean level2Unlocked = false;
    public static boolean level3Unlocked = false;

    private Image restartBlur;
    private final Image heartImage;
    private final Image bulletHoleImage;
    private final Image mainMenuBg;

    private long lastToggleTime = 0;

    public Game() {
        instance = this;

        int highestUnlocked = StatsManager.loadProgression();
        if (highestUnlocked >= 2) level2Unlocked = true;
        if (highestUnlocked >= 3) level3Unlocked = true;

        world = new Level1();

        bulletHoleImage = Toolkit.getDefaultToolkit().getImage("data/ui/bullethole.png");
        heartImage = Toolkit.getDefaultToolkit().getImage("data/ui/heart.png");
        mainMenuBg = Toolkit.getDefaultToolkit().getImage("data/ui/main_menu.png");

        // [Java Topic: Anonymous Inner Classes] We instantiate a UserView and immediately override its paint methods
        view = new UserView(world, 800, 600) {

            private final Font hudFont = new Font("Arial", Font.BOLD, 18);
            private final Font labelFont = new Font("Arial", Font.ITALIC, 22);
            private final Font countdownFont = new Font("SansSerif", Font.BOLD, 150);
            private final Font gameOverLivesFont = new Font("Arial", Font.BOLD, 60);
            private final Font gameOverTimeFont = new Font("Arial", Font.BOLD, 50);
            private final Font promptFont = new Font("Arial", Font.ITALIC, 16);

            private final BasicStroke stroke3 = new BasicStroke(3.0f);
            private final BasicStroke stroke5 = new BasicStroke(5.0f);
            private final BasicStroke stroke6 = new BasicStroke(6.0f);

            private void drawOutlinedText(Graphics2D g, String text, int x, int y, Color fillColor, BasicStroke stroke) {
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                FontRenderContext frc = g.getFontRenderContext();
                TextLayout textLayout = new TextLayout(text, g.getFont(), frc);
                java.awt.geom.AffineTransform transform = java.awt.geom.AffineTransform.getTranslateInstance(x, y);
                Shape outlineShape = textLayout.getOutline(transform);
                g.setColor(Color.BLACK);
                g.setStroke(stroke);
                g.draw(outlineShape);
                g.setColor(fillColor);
                g.fill(outlineShape);
            }

            @Override
            protected void paintBackground(Graphics2D g) {
                if (world.isInMainMenu()) {
                    if (mainMenuBg != null) {
                        g.drawImage(mainMenuBg, 0, 0, getWidth(), getHeight(), this);
                    } else {
                        g.setColor(Color.WHITE);
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                    return;
                }

                g.drawImage(BackgroundManager.getCurrentImage(), 0, 0, getWidth(), getHeight(), this);

                if (!world.isGameOver()) {
                    super.paintBackground(g);
                }

                // [Java Topic: Iteration] Loop through active bullet holes to draw and fade them out
                for (BulletHole b : world.getBulletHoles()) {
                    Point2D.Float p = this.worldToView(b.getPosition());
                    float age = b.getAge();
                    float alpha = 1.0f;
                    if (age > 2.0f) {
                        alpha = 1.0f - (age - 2.0f);
                        if (alpha < 0) alpha = 0;
                    }
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    g.drawImage(bulletHoleImage, (int)p.x - 30, (int)p.y - 30, 60, 60, this);
                }
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }

            @Override
            protected void paintForeground(Graphics2D g) {
                if (world.isInMainMenu() || world.isPaused()) return;

                if (!world.isGameOver() && world.getStartTimer() <= 0) {

                    g.setFont(labelFont);
                    String lbl = "l1_static";
                    if (world.getClass().getSimpleName().equals("Level2")) lbl = "l2_dynamic";
                    if (world.getClass().getSimpleName().equals("Level3")) lbl = "l3_switch";
                    drawOutlinedText(g, lbl, 15, 30, new Color(220, 220, 220), stroke3);

                    g.setFont(hudFont);
                    FontMetrics fm = g.getFontMetrics();
                    int yPos = 30;
                    int screenCenter = getWidth() / 2;
                    int spacing = 180;

                    String scoreText = "Score: " + world.getScore();
                    String timeText = "Time: " + world.getTime();
                    String accText = "Accuracy: " + world.getAccuracy() + "%";

                    drawOutlinedText(g, timeText, screenCenter - (fm.stringWidth(timeText) / 2), yPos, Color.WHITE, stroke3);
                    drawOutlinedText(g, scoreText, (screenCenter - spacing) - (fm.stringWidth(scoreText) / 2), yPos, Color.WHITE, stroke3);
                    drawOutlinedText(g, accText, (screenCenter + spacing) - (fm.stringWidth(accText) / 2), yPos, Color.WHITE, stroke3);

                    int heartSize = 20;
                    int heartGap = 5;
                    int totalLivesWidth = (world.getLives() * (heartSize + heartGap)) - heartGap;
                    int startX = screenCenter - (totalLivesWidth / 2);
                    int startY = 55;
                    for (int i = 0; i < world.getLives(); i++) g.drawImage(heartImage, startX + (i * (heartSize + heartGap)), startY, heartSize, heartSize, this);

                    // [Java Topic: Type Casting & instanceof] Verify the world is Level3 before treating it as one
                    if (world instanceof Level3) {
                        for (SwitchingTarget t : ((Level3)world).getActiveTargets()) {
                            Point2D.Float p = this.worldToView(t.getPosition());
                            int barWidth = 40;
                            int barHeight = 6;
                            int x = (int)p.x - barWidth/2;
                            int y = (int)p.y - 35;

                            g.setColor(Color.RED);
                            g.fillRect(x, y, barWidth, barHeight);
                            g.setColor(Color.GREEN);
                            g.fillRect(x, y, (int)(barWidth * (t.getHealth() / 100f)), barHeight);
                            g.setColor(Color.BLACK);
                            g.drawRect(x, y, barWidth, barHeight);
                        }
                    }
                }

                if (world.getStartTimer() > 0) {
                    if (restartBlur != null) {
                        g.drawImage(restartBlur, 0, 0, getWidth(), getHeight(), this);
                        g.setColor(new Color(0, 0, 0, 100));
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                    g.setFont(countdownFont);
                    String num = "" + world.getStartTimer();
                    FontMetrics fm = g.getFontMetrics();
                    drawOutlinedText(g, num, (getWidth() - fm.stringWidth(num)) / 2, (getHeight() + fm.getAscent()) / 2 - 20, Color.WHITE, stroke6);
                } else {
                    restartBlur = null;
                }

                if (world.isGameOver()) {
                    boolean isOutOfLives = (world.getLives() <= 0);
                    String titleText = isOutOfLives ? "OUT OF LIVES!" : "TIME UP!";
                    Color titleColor = isOutOfLives ? Color.RED : Color.WHITE;

                    g.setFont(isOutOfLives ? gameOverLivesFont : gameOverTimeFont);
                    FontMetrics fmTitle = g.getFontMetrics();
                    drawOutlinedText(g, titleText, (getWidth() - fmTitle.stringWidth(titleText)) / 2, 100, titleColor, stroke5);

                    int lvlNum = 1;
                    String lbl = "l1_static";
                    if (world.getClass().getSimpleName().equals("Level2")) { lvlNum = 2; lbl = "l2_dynamic"; }
                    if (world.getClass().getSimpleName().equals("Level3")) { lvlNum = 3; lbl = "l3_switch"; }

                    String statsTitle = "Final Score: " + world.getScore() + "   |   Accuracy: " + world.getAccuracy() + "%   |   " + lbl;
                    GraphRenderer.drawGraph(g, 150, 150, 500, 300, StatsManager.getRecentScores(lvlNum), statsTitle);

                    g.setFont(promptFont);
                    FontMetrics fmPrompt = g.getFontMetrics();
                    drawOutlinedText(g, "Press F3 to Restart", (getWidth() - fmPrompt.stringWidth("Press F3 to Restart")) / 2, 520, Color.WHITE, stroke3);
                }
            }
        };

        updateCursor();

        // [Java Topic: Event Listeners] Attach keyboard and mouse handlers to listen for player inputs
        mouseController = new MouseHandler(view, world.getPlayer());
        view.addMouseMotionListener(mouseController);
        view.addMouseListener(mouseController);
        view.addKeyListener(new KeyboardHandler(this));
        view.setFocusable(true);
        view.requestFocus();

        // [Java Topic: Swing GUI] Building the actual window you see
        final JFrame frame = new JFrame("aim_trainer.exe");
        JLayeredPane layers = new JLayeredPane();
        layers.setPreferredSize(new Dimension(800, 600));
        view.setBounds(0, 0, 800, 600);
        layers.add(view, JLayeredPane.DEFAULT_LAYER);

        overlay = new GameOverlay(this);
        overlay.setBounds(0, 0, 800, 600);
        layers.add(overlay, JLayeredPane.PALETTE_LAYER);

        frame.add(layers);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationByPlatform(true);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);

        world.start();
    }

    // [Java Topic: Exception Handling] Tries to load the custom cursor, defaults back to standard if it fails
    public void updateCursor() {
        try {
            Image cursorImage = CrosshairManager.getCurrentImage();
            Image scaledCursor = cursorImage.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            Cursor customCursor = Toolkit.getDefaultToolkit().createCustomCursor(scaledCursor, new java.awt.Point(16, 16), "Crosshair");
            view.setCursor(customCursor);
        } catch (Exception e) {
            view.setCursor(Cursor.getDefaultCursor());
        }
    }

    public void updateBackground() { if (view != null) view.repaint(); }

    public void startActualGame(int levelNum) {
        world.stop();

        view.removeMouseListener(mouseController);
        view.removeMouseMotionListener(mouseController);

        if (levelNum == 1) {
            world = new Level1();
            BackgroundManager.setIndex(0);
            TargetManager.setIndex(0);
            SoundManager.playMusic(1);
        }
        else if (levelNum == 2) {
            world = new Level2();
            BackgroundManager.setIndex(1);
            TargetManager.setIndex(1);
            SoundManager.playMusic(2);
        }
        else {
            world = new Level3();
            BackgroundManager.setIndex(2);
            TargetManager.setIndex(2);
            SoundManager.playMusic(3);
        }

        view.setWorld(world);

        mouseController = new MouseHandler(view, world.getPlayer());
        view.addMouseMotionListener(mouseController);
        view.addMouseListener(mouseController);

        world.setInMainMenu(false);
        overlay.hideAll();
        restartBlur = null;

        updateBackground();
        updateCursor();
        world.restartLevel();
    }

    public void returnToMainMenu() {
        world.setInMainMenu(true);
        world.setPaused(false);
        restartBlur = null;

        overlay.setCanGoNextLevel(false);

        world.resetLevelState();

        SoundManager.stopMusic();

        view.repaint();
        overlay.showTitleScreenState();
        view.setCursor(Cursor.getDefaultCursor());
    }

    public void restartGame() {
        if (world.getStartTimer() > 0) return;

        overlay.setCanGoNextLevel(false);
        overlay.hideAll();

        BufferedImage screenshot = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = screenshot.createGraphics();
        view.paint(g2);
        g2.dispose();
        restartBlur = screenshot;
        world.restartLevel();
    }

    public void toggleMenu() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastToggleTime < 200) return;
        lastToggleTime = currentTime;

        if (world.isInMainMenu()) {
            if (overlay.isSettingsVisible()) overlay.showTitleScreenState();
            return;
        }
        if (world.getStartTimer() > 0) return;

        boolean isPaused = world.isPaused();
        if (!isPaused) {
            world.hideTargets();
            BufferedImage screenshot = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = screenshot.createGraphics();
            view.paint(g2);
            g2.dispose();
            overlay.setScreenshot(screenshot);

            world.setPaused(true);
            SoundManager.pauseMusic();

            overlay.showPauseMenuState();
            view.setCursor(Cursor.getDefaultCursor());
        } else {
            world.setPaused(false);
            world.restoreTargets();
            overlay.hideAll();

            SoundManager.resumeMusic();

            updateCursor();
        }
    }

    public void onLevelCompleted() {
        overlay.setVisible(true);
        overlay.hideAllPanels();

        overlay.setCanGoNextLevel(true);
    }

    public void updateTargets() {
        world.refreshTargets();
        view.repaint();
    }

    public GameLevel getWorld() { return world; }

    public static void main(String[] args) { new Game(); }
}
/**
 * SUMMARY: Handles all the 2D user interface screens, buttons, and menus
 * overlaid on top of the physics game.
 */
package game.ui;

import game.core.*;
import game.managers.*;
import game.input.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class GameOverlay extends JPanel {

    private final Game game;

    private JPanel mainMenuPanel;
    private JPanel confirmationPanel;
    private JPanel levelSelectPanel;

    private JPanel settingsRootPanel;
    private JPanel audioPanel;
    private JPanel gamePanel;
    private JPanel crosshairPanel;
    private JPanel targetPanel;
    private JPanel backgroundPanel;
    private JPanel soundsPanel;
    private JPanel controlsPanel;

    private JButton settingsBtn;
    private JButton homeBtn;
    private Image blurredBackground;

    private JButton lvl1Btn;
    private JButton lvl2Btn;
    private JButton lvl3Btn;

    private final ArrayList<JButton> currentNavigableButtons = new ArrayList<>();
    private int selectedButtonIndex = 0;
    private final HoverHandler hoverHandler = new HoverHandler();
    private int savedRootIndex = 0;
    private int savedMainMenuIndex = 0;
    private long lastNavTime = 0;

    private boolean isRebinding = false;
    private String actionToRebind = null;
    private JButton buttonBeingRebound = null;

    private ImageIcon iconGearNormal, iconGearHover;
    private ImageIcon iconArrowNormal, iconArrowHover;
    private boolean isOnTitleScreenSettings = false;

    private boolean canGoNextLevel = false;

    // [Java Topic: Enums] A clean way to restrict variables to a small set of predefined options
    private enum OptionType { CROSSHAIR, BACKGROUND, TARGET }

    // [Java Topic: Inner Classes] Creating a customized button subclass inside our UI manager
    private class SelectionOptionButton extends JButton {
        private final int itemIndex;
        private final OptionType type;
        private boolean isHovered = false;

        public SelectionOptionButton(int index, OptionType type) {
            this.itemIndex = index;
            this.type = type;

            switch (type) {
                case CROSSHAIR: this.setPreferredSize(new Dimension(90, 100)); break;
                case TARGET: this.setPreferredSize(new Dimension(100, 110)); break;
                default: this.setPreferredSize(new Dimension(120, 110)); break;
            }

            this.setContentAreaFilled(false);
            this.setBorderPainted(false);
            this.setFocusable(false);
            this.setCursor(new Cursor(Cursor.HAND_CURSOR));

            this.addActionListener(ignored -> {
                switch (type) {
                    case CROSSHAIR: CrosshairManager.setIndex(itemIndex); game.updateCursor(); break;
                    case BACKGROUND: BackgroundManager.setIndex(itemIndex); game.updateBackground(); break;
                    case TARGET: TargetManager.setIndex(itemIndex); game.updateTargets(); break;
                }
                SwingUtilities.getAncestorOfClass(JPanel.class, this).repaint();
            });

            this.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent ignored) { isHovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent ignored) { isHovered = false; repaint(); }
            });
        }

        private void drawOption(Graphics2D g2d, Image img, String name, boolean selected, boolean hoverBorder, int x, int y, int w, int h, int pad, int nameY) {
            if (img != null) g2d.drawImage(img, x, y, w, h, this);
            if (selected || hoverBorder) {
                g2d.setColor(selected ? new Color(50, 200, 50, 200) : new Color(200, 200, 200, 150));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRect(x - pad, y - pad, w + (pad * 2), h + (pad * 2));
            }
            if (name != null) {
                g2d.setColor(Color.DARK_GRAY);
                g2d.setFont(new Font("Arial", Font.ITALIC, 12));
                g2d.drawString(name, (getWidth() - g2d.getFontMetrics().stringWidth(name)) / 2, nameY);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            switch (type) {
                case CROSSHAIR:
                    boolean cSelected = (itemIndex == CrosshairManager.getCurrentIndex());
                    CrosshairManager.CrosshairData cData = CrosshairManager.getData(itemIndex);
                    if (cData == null) return;
                    int cSize = (isHovered || cSelected) ? 48 : 32;
                    drawOption(g2d, cData.image, cData.name, cSelected, false, (getWidth() - cSize) / 2, 15, cSize, cSize, 5, 15 + cSize + 25);
                    break;
                case BACKGROUND:
                    boolean bSelected = (itemIndex == BackgroundManager.getCurrentIndex());
                    BackgroundManager.BackgroundData bData = BackgroundManager.getData(itemIndex);
                    if (bData == null) return;
                    drawOption(g2d, bData.thumbnail, bData.name, bSelected, isHovered, (getWidth() - 100) / 2, 10, 100, 75, 2, 10 + 75 + 15);
                    break;
                case TARGET:
                    boolean tSelected = (itemIndex == TargetManager.getCurrentIndex());
                    TargetManager.TargetData tData = TargetManager.getData(itemIndex);
                    if (tData == null) return;
                    int tSize = (isHovered || tSelected) ? 64 : 48;
                    drawOption(g2d, tData.image, tData.name, tSelected, false, (getWidth() - tSize) / 2, 15, tSize, tSize, 5, 15 + tSize + 20);
                    break;
            }
        }
    }

    public GameOverlay(Game game) {
        this.game = game;
        this.setLayout(null);
        this.setOpaque(false);
        this.setVisible(true);

        this.removeAll();

        loadIcons();
        initTopLevelButtons();
        initSettingsPanels();
        initMainMenuPanel();
        initLevelSelectPanel();
        initConfirmationPanel();

        setupKeyboardNavigation();
        setupMouseRebinding();

        showTitleScreenState();
    }

    private void setupMouseRebinding() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isRebinding) {
                    int code = -100 - e.getButton();
                    applyRebind(code);
                    return;
                }

                if ((game.getWorld().isPaused() || game.getWorld().isGameOver()) && !isSettingsVisible() && !mainMenuPanel.isVisible() && !confirmationPanel.isVisible()) {
                    int mx = e.getX();
                    int my = e.getY();

                    if (mx >= 575 && mx <= 635 && my >= 415 && my <= 435) {
                        StatsManager.clearStats();
                        repaint();
                    }
                }
            }
        });
    }

    private void loadIcons() {
        ImageIcon rawGear = new ImageIcon("data/ui/gear.png");
        iconGearNormal = new ImageIcon(rawGear.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
        iconGearHover = new ImageIcon(rawGear.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH));

        ImageIcon rawArrow = new ImageIcon("data/ui/arrow.png");
        iconArrowNormal = new ImageIcon(rawArrow.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
        iconArrowHover = new ImageIcon(rawArrow.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH));
    }

    private void initTopLevelButtons() {
        settingsBtn = new JButton(iconGearNormal);
        settingsBtn.setBounds(700, 20, 40, 40);
        styleIconButton(settingsBtn);
        settingsBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent ignored) { settingsBtn.setIcon(iconGearHover); }
            public void mouseExited(MouseEvent ignored) { if (!isSettingsVisible()) settingsBtn.setIcon(iconGearNormal); }
        });
        settingsBtn.addActionListener(ignored -> toggleSettingsRoot());
        this.add(settingsBtn);

        homeBtn = new JButton(iconArrowNormal);
        homeBtn.setBounds(20, 520, 40, 40);
        styleIconButton(homeBtn);
        homeBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent ignored) { homeBtn.setIcon(iconArrowHover); }
            public void mouseExited(MouseEvent ignored) { homeBtn.setIcon(iconArrowNormal); }
        });
        homeBtn.addActionListener(ignored -> handleBackNavigation());
        this.add(homeBtn);
    }

    public void setCanGoNextLevel(boolean b) {
        this.canGoNextLevel = b;
        if (b) {
            this.requestFocusInWindow();
        }
        this.repaint();
    }

    // [Java Topic: UI Layout Managers] Setting up Border and Grid layouts to arrange menu items neatly
    private JPanel createSelectionPanel(String title, int width, int height, int cols, int hGap, int vGap, int count, OptionType type) {
        JPanel panel = createBasePanel(width, height);
        panel.setLayout(new BorderLayout());

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Arial", Font.BOLD, 16));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setBorder(new EmptyBorder(0, 0, 10, 0));
        panel.add(lbl, BorderLayout.NORTH);

        JPanel container = new JPanel(new GridLayout(0, cols, hGap, vGap));
        container.setOpaque(false);
        for(int i = 0; i < count; i++) {
            container.add(new SelectionOptionButton(i, type));
        }

        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void initSettingsPanels() {
        if (settingsRootPanel != null) this.remove(settingsRootPanel);

        settingsRootPanel = createBasePanel(300, 300);
        settingsRootPanel.setLayout(new GridLayout(3, 1, 0, 10));

        JButton btnGame = createBigMenuButton("GAME");
        btnGame.addActionListener(ignored -> { savedRootIndex = 0; openSubPanel(gamePanel); });

        JButton btnAudio = createBigMenuButton("AUDIO");
        btnAudio.addActionListener(ignored -> { savedRootIndex = 1; openSubPanel(audioPanel); });

        JButton btnControls = createBigMenuButton("CONTROLS");
        btnControls.addActionListener(ignored -> { savedRootIndex = 2; openSubPanel(controlsPanel); });

        settingsRootPanel.add(btnGame);
        settingsRootPanel.add(btnAudio);
        settingsRootPanel.add(btnControls);
        this.add(settingsRootPanel);

        audioPanel = createBasePanel(300, 300);
        audioPanel.setLayout(new BoxLayout(audioPanel, BoxLayout.Y_AXIS));
        addLabel(audioPanel, "Volume Control", true);

        JSlider volSlider = new JSlider(0, 10, 5);
        volSlider.setMajorTickSpacing(1);
        volSlider.setPaintTicks(true);
        volSlider.setFocusable(false);
        volSlider.addChangeListener(ignored -> game.getWorld().setMasterVolume(volSlider.getValue() / 10.0f));
        audioPanel.add(volSlider);

        JCheckBox muteBox = new JCheckBox("Mute Sound");
        muteBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        muteBox.setFocusable(false);
        muteBox.addActionListener(ignored -> game.getWorld().setMuted(muteBox.isSelected()));
        audioPanel.add(muteBox);
        this.add(audioPanel);

        gamePanel = createBasePanel(300, 400);
        gamePanel.setLayout(new GridLayout(3, 1, 0, 15)); // Changed to 3 rows
        gamePanel.setBorder(BorderFactory.createCompoundBorder(gamePanel.getBorder(), new EmptyBorder(30, 20, 30, 20)));

        JButton btnCrosshair = createBigMenuButton("CROSSHAIRS");
        btnCrosshair.addActionListener(ignored -> openSubPanel(crosshairPanel));

        JButton btnTarget = createBigMenuButton("TARGETS");
        btnTarget.addActionListener(ignored -> openSubPanel(targetPanel));

        JButton btnBackground = createBigMenuButton("MAPS");
        btnBackground.addActionListener(ignored -> openSubPanel(backgroundPanel));

        gamePanel.add(btnCrosshair);
        gamePanel.add(btnTarget);
        gamePanel.add(btnBackground);
        this.add(gamePanel);

        crosshairPanel = createSelectionPanel("Select Crosshair", 360, 300, 3, 10, 10, CrosshairManager.getCount(), OptionType.CROSSHAIR);
        this.add(crosshairPanel);

        targetPanel = createSelectionPanel("Select Target Skin", 400, 350, 3, 10, 10, TargetManager.getCount(), OptionType.TARGET);
        this.add(targetPanel);

        backgroundPanel = createSelectionPanel("Select Map", 400, 350, 2, 15, 15, BackgroundManager.getCount(), OptionType.BACKGROUND);
        this.add(backgroundPanel);

        soundsPanel = createBasePanel(300, 300);
        soundsPanel.setLayout(new BoxLayout(soundsPanel, BoxLayout.Y_AXIS));
        addLabel(soundsPanel, "Custom Sounds", true);
        addLabel(soundsPanel, "(Coming Soon)", false);
        this.add(soundsPanel);

        controlsPanel = createBasePanel(300, 350);
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        addLabel(controlsPanel, "Keybinds", true);

        addKeybindRow(controlsPanel, "Shoot:", "SHOOT", Keybinds.SHOOT);
        addKeybindRow(controlsPanel, "Pause:", "PAUSE", Keybinds.PAUSE);
        addKeybindRow(controlsPanel, "Restart:", "RESTART", Keybinds.RESTART);

        addLabel(controlsPanel, "(Click button to rebind)", false);
        this.add(controlsPanel);
    }

    private void addKeybindRow(JPanel panel, String labelText, String actionID, int initialKeyCode) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER));
        row.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));

        String keyName = Keybinds.getKeyText(initialKeyCode);
        JButton btnKey = new JButton(keyName);
        btnKey.setFont(new Font("Arial", Font.PLAIN, 14));
        btnKey.setFocusable(false);
        btnKey.setBackground(Color.WHITE);
        btnKey.setPreferredSize(new Dimension(120, 30));

        btnKey.addActionListener(ignored -> startRebind(actionID, btnKey));

        row.add(lbl);
        row.add(btnKey);
        panel.add(row);
    }

    private void startRebind(String actionID, JButton btn) {
        if (isRebinding) return;
        isRebinding = true;
        actionToRebind = actionID;
        buttonBeingRebound = btn;

        btn.setText("Press Key...");
        btn.setBackground(Color.CYAN);
    }

    private void applyRebind(int keyCode) {
        if (!isRebinding) return;
        switch (actionToRebind) {
            case "PAUSE": Keybinds.PAUSE = keyCode; break;
            case "RESTART": Keybinds.RESTART = keyCode; break;
            case "SHOOT": Keybinds.SHOOT = keyCode; break;
        }
        String text = Keybinds.getKeyText(keyCode);
        buttonBeingRebound.setText(text);
        isRebinding = false;
        actionToRebind = null;
        buttonBeingRebound = null;
        refreshButtonVisuals();
    }

    private void handleBackNavigation() {
        if (crosshairPanel.isVisible() || soundsPanel.isVisible() || backgroundPanel.isVisible() || targetPanel.isVisible()) {
            openSubPanel(gamePanel);
        }
        else if (audioPanel.isVisible() || gamePanel.isVisible() || controlsPanel.isVisible()) {
            openSubPanel(settingsRootPanel);
        }
        else if (settingsRootPanel.isVisible() || levelSelectPanel.isVisible()) {
            if (isOnTitleScreenSettings) showTitleScreenState(); else showPauseMenuState();
        }
        else if (!mainMenuPanel.isVisible() && this.isVisible()) {
            showConfirmationPanel();
        }
    }

    private void initMainMenuPanel() {
        if (mainMenuPanel != null) this.remove(mainMenuPanel);
        mainMenuPanel = new JPanel();
        mainMenuPanel.setLayout(new BoxLayout(mainMenuPanel, BoxLayout.Y_AXIS));

        mainMenuPanel.setBounds(150, 180, 500, 400);
        mainMenuPanel.setOpaque(false);

        JLabel title = new JLabel("SAM's AIM TRAINER") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                java.awt.font.FontRenderContext frc = g2.getFontRenderContext();
                java.awt.font.TextLayout textLayout = new java.awt.font.TextLayout(getText(), getFont(), frc);

                float x = (float) ((getWidth() - textLayout.getBounds().getWidth()) / 2);
                float y = textLayout.getAscent();

                java.awt.geom.AffineTransform transform = java.awt.geom.AffineTransform.getTranslateInstance(x, y);
                java.awt.Shape outlineShape = textLayout.getOutline(transform);

                g2.setStroke(new BasicStroke(4.0f));
                g2.setColor(Color.BLACK);
                g2.draw(outlineShape);

                g2.setColor(Color.WHITE);
                g2.fill(outlineShape);
                g2.dispose();
            }
        };
        title.setFont(new Font("Arial", Font.BOLD, 46));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setPreferredSize(new Dimension(500, 60));
        title.setMaximumSize(new Dimension(500, 60));

        mainMenuPanel.add(title);
        mainMenuPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        JLabel inst1 = new JLabel("Aim quickly and shoot the targets.") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                java.awt.font.FontRenderContext frc = g2.getFontRenderContext();
                java.awt.font.TextLayout textLayout = new java.awt.font.TextLayout(getText(), getFont(), frc);
                float x = (float) ((getWidth() - textLayout.getBounds().getWidth()) / 2);
                float y = textLayout.getAscent();
                java.awt.geom.AffineTransform transform = java.awt.geom.AffineTransform.getTranslateInstance(x, y);
                java.awt.Shape outlineShape = textLayout.getOutline(transform);

                g2.setStroke(new BasicStroke(3.0f));
                g2.setColor(Color.BLACK);
                g2.draw(outlineShape);
                g2.setColor(Color.WHITE);
                g2.fill(outlineShape);
                g2.dispose();
            }
        };
        inst1.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 18));
        inst1.setPreferredSize(new Dimension(500, 30));
        inst1.setMaximumSize(new Dimension(500, 30));
        inst1.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainMenuPanel.add(inst1);

        mainMenuPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        JLabel inst2 = new JLabel("Try to get the highest score possible!") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                java.awt.font.FontRenderContext frc = g2.getFontRenderContext();
                java.awt.font.TextLayout textLayout = new java.awt.font.TextLayout(getText(), getFont(), frc);
                float x = (float) ((getWidth() - textLayout.getBounds().getWidth()) / 2);
                float y = textLayout.getAscent();
                java.awt.geom.AffineTransform transform = java.awt.geom.AffineTransform.getTranslateInstance(x, y);
                java.awt.Shape outlineShape = textLayout.getOutline(transform);

                g2.setStroke(new BasicStroke(3.0f));
                g2.setColor(Color.BLACK);
                g2.draw(outlineShape);
                g2.setColor(new Color(255, 215, 0));
                g2.fill(outlineShape);
                g2.dispose();
            }
        };
        inst2.setFont(new Font("Arial", Font.BOLD, 20));
        inst2.setPreferredSize(new Dimension(500, 30));
        inst2.setMaximumSize(new Dimension(500, 30));
        inst2.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainMenuPanel.add(inst2);

        mainMenuPanel.add(Box.createRigidArea(new Dimension(0, 60)));

        JButton playBtn = createTitleScreenButton("PLAY");
        playBtn.addActionListener(ignored -> { savedMainMenuIndex = 0; showLevelSelectMenu(); });
        mainMenuPanel.add(playBtn);
        mainMenuPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton menuSettingsBtn = createTitleScreenButton("SETTINGS");
        menuSettingsBtn.addActionListener(ignored -> { savedMainMenuIndex = 1; showTitleScreenSettings(); });
        mainMenuPanel.add(menuSettingsBtn);
        mainMenuPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton quitBtn = createTitleScreenButton("QUIT");
        quitBtn.addActionListener(ignored -> { savedMainMenuIndex = 2; System.exit(0); });
        mainMenuPanel.add(quitBtn);

        this.add(mainMenuPanel);
    }

    private void initLevelSelectPanel() {
        if (levelSelectPanel != null) this.remove(levelSelectPanel);
        levelSelectPanel = createBasePanel(300, 360);
        levelSelectPanel.setLayout(new BoxLayout(levelSelectPanel, BoxLayout.Y_AXIS));

        addLabel(levelSelectPanel, "SELECT LEVEL", true);
        levelSelectPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        lvl1Btn = createBigMenuButton("l1_static");
        lvl1Btn.setFont(new Font("Arial", Font.ITALIC, 18));
        lvl1Btn.addActionListener(ignored -> game.startActualGame(1));
        levelSelectPanel.add(lvl1Btn);
        levelSelectPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        lvl2Btn = createBigMenuButton("l2_dynamic (LOCKED)");
        lvl2Btn.setFont(new Font("Arial", Font.ITALIC, 18));
        lvl2Btn.addActionListener(ignored -> {
            if (Game.level2Unlocked) game.startActualGame(2);
        });
        levelSelectPanel.add(lvl2Btn);
        levelSelectPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        lvl3Btn = createBigMenuButton("l3_switch (LOCKED)");
        lvl3Btn.setFont(new Font("Arial", Font.ITALIC, 18));
        lvl3Btn.addActionListener(ignored -> {
            if (Game.level3Unlocked) game.startActualGame(3);
        });
        levelSelectPanel.add(lvl3Btn);

        this.add(levelSelectPanel);
    }

    public void showLevelSelectMenu() {
        hideAllPanels();
        levelSelectPanel.setVisible(true);
        settingsBtn.setVisible(false);
        homeBtn.setVisible(true);
        isOnTitleScreenSettings = true;

        if (Game.level2Unlocked) {
            lvl2Btn.setText("l2_dynamic");
            lvl2Btn.setForeground(Color.BLACK);
        } else {
            lvl2Btn.setText("l2_dynamic (LOCKED)");
            lvl2Btn.setForeground(Color.GRAY);
        }

        if (Game.level3Unlocked) {
            lvl3Btn.setText("l3_switch");
            lvl3Btn.setForeground(Color.BLACK);
        } else {
            lvl3Btn.setText("l3_switch (LOCKED)");
            lvl3Btn.setForeground(Color.GRAY);
        }

        updateNavigableButtons(levelSelectPanel);
    }

    private void initConfirmationPanel() {
        if (confirmationPanel != null) this.remove(confirmationPanel);
        confirmationPanel = createBasePanel(300, 250);
        confirmationPanel.setBounds(250, 175, 300, 250);
        confirmationPanel.setLayout(new BoxLayout(confirmationPanel, BoxLayout.Y_AXIS));
        confirmationPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        JLabel confirmText = new JLabel("Back to Main Menu?");
        confirmText.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmText.setFont(new Font("Arial", Font.BOLD, 18));
        confirmationPanel.add(confirmText);
        confirmationPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        JButton yesBtn = createTitleScreenButton("YES");
        yesBtn.setBackground(new Color(255, 100, 100));
        yesBtn.addActionListener(ignored -> game.returnToMainMenu());
        confirmationPanel.add(yesBtn);
        confirmationPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        JButton noBtn = createTitleScreenButton("NO");
        noBtn.addActionListener(ignored -> hideConfirmationPanel());
        confirmationPanel.add(noBtn);
        this.add(confirmationPanel);
    }

    private JPanel createBasePanel(int width, int height) {
        JPanel p = new JPanel();
        p.setBounds((800 - width) / 2, (600 - height) / 2, width, height);
        p.setBackground(new Color(255, 255, 255, 255));
        p.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        p.setVisible(false);
        p.setBorder(BorderFactory.createCompoundBorder(p.getBorder(), new EmptyBorder(15, 15, 15, 15)));
        return p;
    }

    private void styleIconButton(JButton btn) {
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusable(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JButton createBigMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setFocusable(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(Color.LIGHT_GRAY);
        btn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        return btn;
    }

    private JButton createTitleScreenButton(String text) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setFocusable(false);
        btn.setPreferredSize(new Dimension(160, 40));
        btn.setMaximumSize(new Dimension(160, 40));
        btn.setBackground(Color.LIGHT_GRAY);
        btn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        return btn;
    }

    private void addLabel(JPanel panel, String text, boolean isHeader) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (isHeader) {
            label.setFont(new Font("Arial", Font.BOLD, 16));
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        } else {
            label.setFont(new Font("Arial", Font.PLAIN, 13));
            label.setForeground(Color.DARK_GRAY);
        }
        panel.add(label);
    }

    public void hideAllPanels() {
        mainMenuPanel.setVisible(false);
        settingsRootPanel.setVisible(false);
        audioPanel.setVisible(false);
        gamePanel.setVisible(false);
        controlsPanel.setVisible(false);
        confirmationPanel.setVisible(false);
        if (levelSelectPanel != null) levelSelectPanel.setVisible(false);
        if (crosshairPanel != null) crosshairPanel.setVisible(false);
        if (targetPanel != null) targetPanel.setVisible(false);
        if (backgroundPanel != null) backgroundPanel.setVisible(false);
        if (soundsPanel != null) soundsPanel.setVisible(false);
        if (isRebinding) isRebinding = false;
    }

    private void openSubPanel(JPanel panelToShow) {
        hideAllPanels();
        panelToShow.setVisible(true);
        if (!isOnTitleScreenSettings) {
            settingsBtn.setVisible(true);
            settingsBtn.setIcon(iconGearHover);
        } else {
            settingsBtn.setVisible(false);
        }
        homeBtn.setVisible(true);

        if (panelToShow == settingsRootPanel) {
            updateNavigableButtons(settingsRootPanel);
            selectedButtonIndex = savedRootIndex;
            refreshButtonVisuals();
        } else if (panelToShow == controlsPanel) {
            updateNavigableButtons(controlsPanel);
        } else if (panelToShow == gamePanel) {
            updateNavigableButtons(gamePanel);
        }
        else if (panelToShow == crosshairPanel || panelToShow == targetPanel || panelToShow == backgroundPanel) {
            Component centerComp = ((BorderLayout) panelToShow.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (centerComp instanceof JScrollPane scroll) {
                if (scroll.getViewport().getView() instanceof JPanel p) updateNavigableButtons(p);
            }
        }
        else {
            currentNavigableButtons.clear();
            selectedButtonIndex = -1;
        }
    }

    public void showTitleScreenState() {
        hideAllPanels();
        mainMenuPanel.setVisible(true);
        settingsBtn.setVisible(false);
        homeBtn.setVisible(false);
        blurredBackground = null;
        isOnTitleScreenSettings = false;
        this.setVisible(true);
        updateNavigableButtons(mainMenuPanel);
        selectedButtonIndex = savedMainMenuIndex;
        refreshButtonVisuals();
        this.repaint();
    }

    public void showTitleScreenSettings() {
        hideAllPanels();
        settingsRootPanel.setVisible(true);
        settingsBtn.setVisible(false);
        homeBtn.setVisible(true);
        isOnTitleScreenSettings = true;
        savedRootIndex = 0;
        this.setVisible(true);
        updateNavigableButtons(settingsRootPanel);
    }

    public void showPauseMenuState() {
        hideAllPanels();
        settingsBtn.setVisible(true);
        settingsBtn.setIcon(iconGearNormal);
        homeBtn.setVisible(true);
        isOnTitleScreenSettings = false;
        savedRootIndex = 0;
        this.setVisible(true);
        currentNavigableButtons.clear();
        selectedButtonIndex = 0;
        refreshButtonVisuals();
    }

    private void toggleSettingsRoot() {
        if (isSettingsVisible()) {
            hideAllPanels();
            if (!isOnTitleScreenSettings && game.getWorld().isPaused()) showPauseMenuState();
        } else {
            savedRootIndex = 0;
            openSubPanel(settingsRootPanel);
        }
    }

    public void showConfirmationPanel() {
        hideAllPanels();
        settingsBtn.setVisible(false);
        homeBtn.setVisible(false);
        confirmationPanel.setVisible(true);
        updateNavigableButtons(confirmationPanel);
    }

    public void hideConfirmationPanel() {
        confirmationPanel.setVisible(false);
        if(isOnTitleScreenSettings) openSubPanel(settingsRootPanel); else showPauseMenuState();
    }

    private class HoverHandler extends MouseAdapter {
        @Override
        public void mouseEntered(MouseEvent e) {
            Object source = e.getSource();
            int index = currentNavigableButtons.indexOf((JButton) source);
            if (index != -1 && index != selectedButtonIndex) {
                selectedButtonIndex = index;
                refreshButtonVisuals();
            }
        }
    }

    private void navigateGrid(int code, int columns) {
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
            selectedButtonIndex--;
            if (selectedButtonIndex < 0) selectedButtonIndex = currentNavigableButtons.size() - 1;
        } else if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
            selectedButtonIndex++;
            if (selectedButtonIndex >= currentNavigableButtons.size()) selectedButtonIndex = 0;
        } else if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
            selectedButtonIndex -= columns;
            if (selectedButtonIndex < 0) selectedButtonIndex += currentNavigableButtons.size();
        } else if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
            selectedButtonIndex += columns;
            if (selectedButtonIndex >= currentNavigableButtons.size()) selectedButtonIndex -= currentNavigableButtons.size();
        }
        refreshButtonVisuals();
    }

    private void setupKeyboardNavigation() {
        this.setFocusable(true);
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isRebinding) { applyRebind(e.getKeyCode()); return; }

                long currentTime = System.currentTimeMillis();
                if (currentTime - lastNavTime < 150) return;
                lastNavTime = currentTime;

                int code = e.getKeyCode();

                if (code == KeyEvent.VK_ESCAPE && isVisible()) {
                    if (confirmationPanel.isVisible()) { hideConfirmationPanel(); return; }
                    if (isSettingsVisible()) { handleBackNavigation(); return; }
                    if (mainMenuPanel.isVisible()) { return; }
                    game.toggleMenu(); return;
                }

                if (canGoNextLevel && code == KeyEvent.VK_SPACE) {
                    int cl = 1;
                    if (game.getWorld().getClass().getSimpleName().equals("Level2")) cl = 2;
                    if (game.getWorld().getClass().getSimpleName().equals("Level3")) cl = 3;
                    if (cl < 3) {
                        game.startActualGame(cl + 1);
                        canGoNextLevel = false;
                        repaint();
                    }
                    return;
                }

                if (currentNavigableButtons.isEmpty()) return;

                if (crosshairPanel != null && crosshairPanel.isVisible() || targetPanel != null && targetPanel.isVisible()) {
                    navigateGrid(code, 3);
                } else if (backgroundPanel != null && backgroundPanel.isVisible()) {
                    navigateGrid(code, 2);
                }
                else {
                    if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                        selectedButtonIndex--;
                        if (selectedButtonIndex < 0) selectedButtonIndex = currentNavigableButtons.size() - 1;
                        refreshButtonVisuals();
                    } else if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
                        selectedButtonIndex++;
                        if (selectedButtonIndex >= currentNavigableButtons.size()) selectedButtonIndex = 0;
                        refreshButtonVisuals();
                    }
                }

                if (code == KeyEvent.VK_ENTER) {
                    if (selectedButtonIndex >= 0 && selectedButtonIndex < currentNavigableButtons.size()) {
                        currentNavigableButtons.get(selectedButtonIndex).doClick();
                    }
                }
            }
        });
    }

    private void updateNavigableButtons(JPanel panel) {
        currentNavigableButtons.clear();
        findButtonsRecursively(panel);
        selectedButtonIndex = 0;
        refreshButtonVisuals();
        this.requestFocusInWindow();
    }

    private void findButtonsRecursively(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton btn) {
                if (btn.getText().isEmpty() && !(btn instanceof SelectionOptionButton)) continue;
                currentNavigableButtons.add(btn);
                btn.removeMouseListener(hoverHandler);
                btn.addMouseListener(hoverHandler);
            } else if (c instanceof Container cont) {
                findButtonsRecursively(cont);
            }
        }
    }

    private void refreshButtonVisuals() {
        for (int i = 0; i < currentNavigableButtons.size(); i++) {
            JButton btn = currentNavigableButtons.get(i);
            if (isRebinding && btn == buttonBeingRebound) continue;

            if (btn instanceof SelectionOptionButton sBtn) {
                sBtn.isHovered = (i == selectedButtonIndex);
                sBtn.repaint();
                if (i == selectedButtonIndex) {
                    if ((sBtn.type == OptionType.CROSSHAIR && crosshairPanel.isVisible()) ||
                            (sBtn.type == OptionType.TARGET && targetPanel.isVisible()) ||
                            (sBtn.type == OptionType.BACKGROUND && backgroundPanel.isVisible())) {
                        btn.scrollRectToVisible(new Rectangle(0, 0, btn.getWidth(), btn.getHeight()));
                    }
                }
                continue;
            }

            if (btn.getIcon() != null) {
                continue;
            }

            String text = btn.getText().replace("> ", "").replace(" <", "");
            boolean isHighlightedButton = text.equals("YES");

            if (i == selectedButtonIndex) {
                if (isHighlightedButton) btn.setBackground(new Color(200, 50, 50));
                else btn.setBackground(Color.GRAY);
                btn.setText("> " + text + " <");
            } else {
                if (isHighlightedButton) btn.setBackground(new Color(255, 100, 100));
                else btn.setBackground(Color.LIGHT_GRAY);
                btn.setText(text);
            }
        }
    }

    public void hideAll() {
        hideAllPanels();
        if (settingsBtn != null) settingsBtn.setVisible(false);
        if (homeBtn != null) homeBtn.setVisible(false);
        blurredBackground = null;

        if (canGoNextLevel) {
            this.setVisible(true);
        } else {
            this.setVisible(false);
        }
    }

    public boolean isSettingsVisible() {
        return settingsRootPanel.isVisible() || audioPanel.isVisible() ||
                gamePanel.isVisible() || controlsPanel.isVisible() ||
                (crosshairPanel != null && crosshairPanel.isVisible()) ||
                (targetPanel != null && targetPanel.isVisible()) ||
                (backgroundPanel != null && backgroundPanel.isVisible()) ||
                (soundsPanel != null && soundsPanel.isVisible()) ||
                (levelSelectPanel != null && levelSelectPanel.isVisible());
    }

    public void setScreenshot(Image img) {
        this.blurredBackground = img;
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (blurredBackground != null) {
            g.drawImage(blurredBackground, 0, 0, getWidth(), getHeight(), this);
            g.setColor(new Color(0, 0, 0, 80));
            g.fillRect(0, 0, getWidth(), getHeight());

            if (game.getWorld().isPaused() && !isSettingsVisible() && !mainMenuPanel.isVisible() && !confirmationPanel.isVisible()) {
                int lvlNum = 1;
                if (game.getWorld().getClass().getSimpleName().equals("Level2")) lvlNum = 2;
                if (game.getWorld().getClass().getSimpleName().equals("Level3")) lvlNum = 3;

                List<Integer> stats = StatsManager.getRecentScores(lvlNum);
                GraphRenderer.drawGraph((Graphics2D)g, 150, 150, 500, 300, stats, "Recent Progress (Level " + lvlNum + ")");
            }
        }

        if (canGoNextLevel && blurredBackground == null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 20));
            String txt = "Press SPACE to move onto next level";

            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(txt)) / 2;
            int ty = 560;

            java.awt.font.TextLayout textLayout = new java.awt.font.TextLayout(txt, g2.getFont(), g2.getFontRenderContext());
            java.awt.geom.AffineTransform transform = java.awt.geom.AffineTransform.getTranslateInstance(tx, ty);
            java.awt.Shape outlineShape = textLayout.getOutline(transform);

            g2.setStroke(new BasicStroke(3.0f));
            g2.setColor(Color.BLACK);
            g2.draw(outlineShape);

            g2.setColor(new Color(255, 215, 0));
            g2.fill(outlineShape);
        }
    }
}
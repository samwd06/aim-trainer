/**
 * SUMMARY: A specialized drawing class that plots your recent scores on a
 * line graph when you pause or finish a level.
 */
// src/game/ui/GraphRenderer.java
package game.ui;

import game.core.*;
import game.managers.*;
import game.input.*;

import java.awt.*;
import java.util.List;
import java.awt.geom.Path2D;

public class GraphRenderer {

    public static void drawGraph(Graphics2D g, int x, int y, int width, int height, List<Integer> scores, String titleStr) {
        // [Java Topic: Java 2D Graphics] Using colors and basic shapes to draw the graph background
        g.setColor(new Color(20, 20, 20, 220));
        g.fillRoundRect(x, y, width, height, 20, 20);
        g.setColor(new Color(200, 200, 200, 150));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, width, height, 20, 20);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(titleStr, x + (width - fm.stringWidth(titleStr)) / 2, y + 35);

        int btnW = 60;
        int btnH = 20;
        int btnX = x + width - btnW - 15;
        int btnY = y + height - btnH - 15;

        g.setColor(new Color(200, 50, 50, 200));
        g.fillRoundRect(btnX, btnY, btnW, btnH, 5, 5);
        g.setColor(new Color(255, 100, 100));
        g.setStroke(new BasicStroke(1));
        g.drawRoundRect(btnX, btnY, btnW, btnH, 5, 5);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        String rstStr = "RESET";
        g.drawString(rstStr, btnX + (btnW - g.getFontMetrics().stringWidth(rstStr))/2, btnY + 14);

        if (scores == null || scores.isEmpty()) {
            String msg = "No recent data available.";
            g.setFont(new Font("Arial", Font.ITALIC, 14));
            g.setColor(Color.LIGHT_GRAY);
            g.drawString(msg, x + (width - g.getFontMetrics().stringWidth(msg)) / 2, y + height / 2);
            return;
        }

        int padX = 50, padY = 40, padTop = 60;
        int plotW = width - 2 * padX;
        int plotH = height - padTop - padY;

        g.setColor(new Color(100, 100, 100));
        g.setStroke(new BasicStroke(2));
        g.drawLine(x + padX, y + height - padY, x + width - padX, y + height - padY);
        g.drawLine(x + padX, y + padTop, x + padX, y + height - padY);

        int maxScore = 1;
        // [Java Topic: Algorithms] Finding the highest score so the graph's roof scales properly
        for (int s : scores) if (s > maxScore) maxScore = s;
        maxScore = ((maxScore / 5) + 1) * 5;

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        for (int i = 0; i <= 5; i++) {
            int val = maxScore * i / 5;
            int yPos = y + height - padY - (int) ((val / (double) maxScore) * plotH);
            g.setColor(new Color(80, 80, 80, 100));
            if (i > 0) g.drawLine(x + padX, yPos, x + width - padX, yPos);
            g.setColor(Color.LIGHT_GRAY);
            String label = String.valueOf(val);
            g.drawString(label, x + padX - g.getFontMetrics().stringWidth(label) - 10, yPos + 4);
        }

        int n = scores.size();
        int[] px = new int[n];
        int[] py = new int[n];
        for (int i = 0; i < n; i++) {
            int step = (n == 1) ? plotW / 2 : (plotW / (n - 1));
            px[i] = x + padX + (n == 1 ? step : i * step);
            py[i] = y + height - padY - (int) ((scores.get(i) / (double) maxScore) * plotH);
        }

        if (n > 1) {
            Path2D.Float path = new Path2D.Float();
            path.moveTo(px[0], y + height - padY);
            for (int i = 0; i < n; i++) path.lineTo(px[i], py[i]);
            path.lineTo(px[n - 1], y + height - padY);
            path.closePath();
            g.setColor(new Color(100, 200, 255, 50));
            g.fill(path);
        }

        g.setColor(new Color(100, 200, 255));
        g.setStroke(new BasicStroke(3));
        for (int i = 0; i < n - 1; i++) g.drawLine(px[i], py[i], px[i + 1], py[i + 1]);

        for (int i = 0; i < n; i++) {
            g.setColor(new Color(30, 30, 30));
            g.fillOval(px[i] - 5, py[i] - 5, 10, 10);
            g.setColor(Color.WHITE);
            g.fillOval(px[i] - 3, py[i] - 3, 6, 6);
        }

        g.setColor(Color.GRAY);
        g.drawString("Recent Plays", x + (width - g.getFontMetrics().stringWidth("Recent Plays")) / 2, y + height - 10);
    }
}
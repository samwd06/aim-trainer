/**
 * SUMMARY: A helper tool to trace images and get polygon points.
 * You don't really need to touch this for the main game, it's just a dev tool.
 *
 * WHY IT WAS NOT USED IN THIS PROJECT:
 * For an "Aim Trainer" game, hitboxes need to be highly predictable and consistent
 * so the player's accuracy is evaluated fairly. Using complex, irregular polygon
 * shapes (like tracing the exact spiky outline of a star or a character) would
 * make hit detection feel random or unfair to the player.
 * Instead, this project uses standard, symmetrical primitive shapes like
 * BoxShape (for Targets) and CircleShape (for the Crosshair/Bullets) to ensure
 * tight, accurate, and fair gameplay mechanics.
 */

package game;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;

// [Java Topic: Inheritance] Extending JPanel to create a custom drawing area
public class PolygonEditor extends JPanel
{   private static double WIDTH = 500;
    private static double HEIGHT = 500;
    private static int SCALE = 8;

    // [Java Topic: Collections] Using a List to keep track of all the points we click
    private List<Point2D.Float> points;
    private int currentVertex;

    private ImageIcon icon;
    private float boxHeight;
    private int powerOf10;

    private double canvasWidth;
    private double canvasHeight;
    private double bitmapWidth;
    private double bitmapHeight;
    private double centreX, centreY;
    private double pixelScale;
    private double scale;

    private PolygonEditor()
    {
        this(null, 1.0f);
    }

    private float round(float x) {
        return Math.round(powerOf10*x)/(float)powerOf10;
    }

    private PolygonEditor(String f, float boxHeight)
    {
        super();
        this.boxHeight = boxHeight;
        System.out.println("height = " + boxHeight);
        powerOf10 = 1;
        // [Java Topic: Loops] Simple while loop to figure out the scaling multiplier
        while (powerOf10*boxHeight < 300) {
            powerOf10 *= 10;
        }

        if (f != null) icon = new ImageIcon(f);
        if (icon == null) {
            pixelScale = SCALE;
            bitmapWidth = WIDTH/SCALE;
            bitmapHeight = HEIGHT/SCALE;
        } else {
            Image image = icon.getImage();
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            pixelScale = Math.min(WIDTH/w, HEIGHT/h);
            bitmapWidth = w;
            bitmapHeight = h;
            image = image.getScaledInstance((int)(w * pixelScale), -1, Image.SCALE_DEFAULT);
            icon.setImage(image);
        }
        canvasWidth = (bitmapWidth * pixelScale);
        canvasHeight = (bitmapHeight * pixelScale);
        centreX = canvasWidth/2.0f;
        centreY = canvasHeight/2.0f;
        scale = boxHeight / canvasHeight;
        setPreferredSize(new java.awt.Dimension((int)canvasWidth, (int)canvasHeight));

        points = new ArrayList<Point2D.Float>();

        // [Java Topic: Anonymous Inner Classes] Quick way to add a mouse listener without making a whole new file
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                Point2D.Float p = toGridPoint(e.getX(), e.getY());
                currentVertex = findVertex(p);
                if (currentVertex < 0) {
                    currentVertex = points.size();
                    points.add(p);
                } else {
                    points.set(currentVertex, p);
                }
                repaint();
            }

            public void mouseReleased(MouseEvent e) {
                String mods = e.getMouseModifiersText(e.getModifiers());
                if (!mods.equals("Button1")) {
                    points.remove(currentVertex);
                }
                currentVertex = -1;
                updateView();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point2D.Float p = toGridPoint(e.getX(), e.getY());
                points.set(currentVertex, p);
                repaint();
            }
        });
    }

    int findVertex(Point2D.Float p) {
        double close = 5*scale;
        for (int i = 0; i < points.size(); ++i) {
            if (points.get(i).distance(p) < close) {
                return i;
            }
        }
        return -1;
    }

    public String toString()
    {
        String s = "";
        // [Java Topic: Enhanced For-Loop] Clean way to go through all the points we saved
        for (Point2D.Float p : points) {
            if (s.length() > 0) s += ", ";
            s += round(p.x) + "f" + "," + round(p.y) + "f";
        }
        return s;
    }

    private Point2D.Float toGridPoint(int x, int y) {
        return new Point2D.Float((float)((x - centreX)*scale), (float)((centreY - y)*scale));
    }

    private Point2D.Float screenPosition(Point2D.Float p) {
        return new Point2D.Float((float)(centreX + p.x/scale), (float)(centreY - p.y/scale));
    }

    // [Java Topic: Method Overriding] Customizing how this panel draws itself
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        for (int i=0; i<this.getHeight(); i+=5)
            for (int j=0; j<this.getWidth(); j+=5){
                if (i % 2 == 0){
                    if (j % 2 == 0)  g.setColor(Color.pink);
                    else g.setColor(Color.yellow);
                }
                else{
                    if (j % 2 == 0)  g.setColor(Color.yellow);
                    else g.setColor(Color.pink);
                }
                g.fillRect(j,i,5,5);
            }
        if (icon != null) icon.paintIcon(this, g, 0, 0);

        g2.setColor(Color.BLUE);
        Point2D.Float prev = null;
        float r = 4;
        for (Point2D.Float gp : points) {
            Point2D.Float p = screenPosition(gp);
            g2.fill(new Ellipse2D.Float(p.x - r, p.y - r, 2*r, 2*r));
            if (prev != null) {
                g2.draw(new Line2D.Float(prev, p));
            }
            prev = p;
        }
    }

    public static void main(String[] args) {
        String fileName = "data/student.png";
        float boxHeight = 4.0f;
        int firstCoordIndex = 0;
        if (args.length > 0) {
            // [Java Topic: Try-Catch blocks] Stops the program from crashing if someone types a letter instead of a number
            try {
                String[] test = args[0].split("[, ]+");
                Float.parseFloat(test[0]);
            } catch (NumberFormatException e) {
                fileName = args[0];
                firstCoordIndex = 1;
                if (args.length > 2 && args[1].toLowerCase().equals("-height")) {
                    boxHeight = Float.parseFloat(args[2]);
                    firstCoordIndex = 3;
                }
            }
        }
        java.util.ArrayList<String> coordStrings = new java.util.ArrayList<String>();
        for (int i = firstCoordIndex; i < args.length; i++) {
            String[] coords = args[i].split("[, ]+");
            for (String x : coords) coordStrings.add(x);
        }
        PolygonEditor editor = new PolygonEditor(fileName, boxHeight);
        if (coordStrings.size() > 0) {
            int i = 0;
            boolean gotX = false;
            float x = 0;
            float y = 0;
            while (i < coordStrings.size()) {
                try {
                    float xy = Float.parseFloat(coordStrings.get(i));
                    if (gotX) {
                        y = xy;
                        Point2D.Float p = new Point2D.Float(x, y);
                        editor.points.add(p);
                        gotX = false;
                    } else {
                        x = xy;
                        gotX = true;
                    }
                } catch (NumberFormatException e) { }
                i++;
            }
        }
        JFrame frame = new JFrame(fileName == null ? "polygon editor" : fileName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(editor);
        frame.pack();
        frame.setVisible(true);
    }

    private void updateView()
    {
        System.out.println(this);
        repaint();
    }

}
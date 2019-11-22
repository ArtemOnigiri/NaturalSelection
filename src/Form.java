import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class Form extends JFrame implements Runnable {

    private final int w = 1920;
    private final int h = 1080;

    private final int FRAMES_TOTAL = 100000;
    private final int SKIP_FRAMES = 1;

    private final Color BG = new Color(200, 200, 200, 255);
    private final Color BLUE = new Color(150, 160, 255, 255);
    private final Color RED = new Color(255, 100, 120, 255);
    private BufferedImage buf = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    private BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    private BufferedImage graph = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    private BufferedImage sprites[] = new BufferedImage[2];
    private final AffineTransform IDENTITY = new AffineTransform();

    private ArrayList<Bacterium> bacteria = new ArrayList<>();
    private ArrayList<Food> food = new ArrayList<>();

    private final int FOOD_RADIUS = 5;
    private final int BACTERIA_RADIUS = 20;
    private int frame = 0;

    public Form() {
        for (int i = 0; i < sprites.length; i++) {
            try {
                sprites[i] = ImageIO.read(new File("img/m" + i + ".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.setSize(w + 16, h + 38);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocation(50, 50);
        this.add(new JLabel(new ImageIcon(img)));
        for(int i = 0; i < 1; i++) {
            Bacterium a = new Bacterium(0, (float)(Math.random() * (w - 100) + 50), (float)(Math.random() * (h - 100) + 50));
            bacteria.add(a);
        }
    }

    @Override
    public void run() {
        while(frame < FRAMES_TOTAL) {
            this.repaint();
        }
    }

    @Override
    public void paint(Graphics g) {
        try {
            drawScene(img);
            drawGraph(graph);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < SKIP_FRAMES; i++) logic();
        Graphics2D g2 = buf.createGraphics();
        g2.drawImage(img, null, 0, 0);
        g2.drawImage(graph, null, 0, 0);
        ((Graphics2D)g).drawImage(buf, null, 8, 30);
    }

    private void drawGraph(BufferedImage image) throws IOException {
        Graphics2D g2 = image.createGraphics();
        if(bacteria.size() > 0) {
            int type0 = (int)bacteria.stream().filter(a -> a.type == 0).count();
            int type1 = bacteria.size() - type0;
            int py = type0;
            if (py > h - 1) py = h - 1;
            int py1 = type1;
            if (py1 > h - 1) py1 = h - 1;
            int px = (int) ((float) frame / FRAMES_TOTAL * (w - 1));
            g2.setColor(BLUE);
            g2.fillRect(px, h - py - 1, 1, py);
            g2.setColor(RED);
            g2.fillRect(px, h - py1 - 450, 1, py1);
//            int[] speeds = new int[100];
//            int[] slips = new int[100];
//            for (Bacterium a : bacteria) {
//                speeds[(int)(a.speed * 10) - 1]++;
//                slips[(int)(a.slip * 100) - 1]++;
//            }
//            g2.setColor(BG);
//            g2.fillRect(0, 0, w, h - 400);

//            for (int i = 0; i < 100; i++) {
//                g2.setColor(Color.getHSBColor(i / 100f, 1f, 0.5f));
//                if(speeds[i] > 0) g2.fillRect(i * 5, h - 400 - speeds[i] * 5, 5, speeds[i] * 5);
//            }
//            ImageIO.write(image, "png", new File("graph/G" + (frame / SKIP_FRAMES) + ".png"));
        }
    }

    private void drawScene(BufferedImage image) throws IOException {
        Graphics2D g2 = image.createGraphics();
        g2.setColor(BG);
        g2.fillRect(0, 0, w, h);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (Food a : food) {
            g2.setColor(Food.COLOR[a.type]);
            g2.fillOval((int) a.x - FOOD_RADIUS, (int) a.y - FOOD_RADIUS, FOOD_RADIUS * 2, FOOD_RADIUS * 2);
        }
        float bacteriaScale = BACTERIA_RADIUS * 0.01f;
        for (Bacterium a : bacteria) {
            float sw = sprites[a.type].getWidth() * 0.5f * bacteriaScale;
            float sh = sprites[a.type].getHeight() * 0.5f * bacteriaScale;
            AffineTransform trans = new AffineTransform();
            trans.setTransform(IDENTITY);
            trans.translate(a.x - sw, a.y - sh);
            trans.rotate(a.rotation + Math.PI / 2, sw, sh);
            trans.scale(bacteriaScale, bacteriaScale);
            g2.drawImage(sprites[a.type], trans, this);
        }
//        ImageIO.write(image, "png", new File("images/T" + (frame / SKIP_FRAMES) + ".png"));
    }

    private void logic() {
        for (Bacterium a : bacteria) {
            a.x += a.sx;
            a.y += a.sy;
            a.sx *= a.slip;
            a.sy *= a.slip;
            if(a.x < 0) {
                a.sx += 5;
            }
            else if(a.x > w) {
                a.sx -= 5;
            }
            if(a.y < 0) {
                a.sy += 5;
            }
            else if(a.y > h) {
                a.sy -= 5;
            }
            double targetAngle = Math.atan2(a.ty, a.tx);
            float rotationForMotion;
            if (targetAngle < 0) targetAngle = targetAngle + (float)(Math.PI * 2.0);
            if ((Math.abs(a.rotation - targetAngle) < a.rotationSpeed) || (Math.abs(a.rotation - targetAngle) > Math.PI * 2 - a.rotationSpeed)) {
                a.rotation = (float)targetAngle;
            }
            else if (((a.rotation < targetAngle) && (a.rotation + 3.1415f > targetAngle)) || ((a.rotation > targetAngle) && (a.rotation - 3.1415f > targetAngle))) {
                a.rotation += a.rotationSpeed;
            }
            else {
                a.rotation -= a.rotationSpeed;
            }
            if(a.rotation < 0) a.rotation += (float)(Math.PI * 2.0);
            else if(a.rotation > Math.PI * 2.0) a.rotation -= (float)(Math.PI * 2.0);
            rotationForMotion = a.rotation;
            if(a.tx * a.tx + a.ty * a.ty > 1) {
                a.sx += (float)Math.cos(rotationForMotion) * a.speed;
                a.sy += (float)Math.sin(rotationForMotion) * a.speed;
            }
            if(a.age > 50) {
                if (a.type == 0) {
                    Food closestFood = null;
                    float minFoodDist = (w * w) + (h * h);
                    for (Food f : food) {
                        if (f.toBeDeleted) continue;
                        float dist2 = (a.x - f.x) * (a.x - f.x) + (a.y - f.y) * (a.y - f.y);
                        if (dist2 < minFoodDist) {
                            minFoodDist = dist2;
                            closestFood = f;
                        }
                    }
                    if (closestFood != null) {
                        a.tx = closestFood.x - a.x;
                        a.ty = closestFood.y - a.y;
                        if (minFoodDist < FOOD_RADIUS * FOOD_RADIUS + BACTERIA_RADIUS * BACTERIA_RADIUS) {
                            closestFood.toBeDeleted = true;
                            a.food++;
                        }
                    }
                } else if (a.type == 1) {
                    Bacterium closestFood = null;
                    float minFoodDist = (w * w) + (h * h);
                    for (Bacterium b : bacteria) {
                        if (b.toBeDeleted) continue;
                        if (b.type != 0) continue;
//                        if (b.food > 3) continue;
                        float dist2 = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
                        if (dist2 < minFoodDist) {
                            minFoodDist = dist2;
                            closestFood = b;
                        }
                    }
                    if (closestFood != null) {
                        a.tx = closestFood.x - a.x;
                        a.ty = closestFood.y - a.y;
                        if (minFoodDist < BACTERIA_RADIUS * BACTERIA_RADIUS + BACTERIA_RADIUS * BACTERIA_RADIUS) {
                            closestFood.toBeDeleted = true;
                            a.food += closestFood.food * 0.1f;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < bacteria.size(); i++) {
            Bacterium a = bacteria.get(i);
            if(a.food >= 6) {
                a.food -= 3;
                int type = a.type;
                if(Math.random() < 0.05) {
                    type = (int)(Math.random() * 2);
                }
                Bacterium b = new Bacterium(type, a.x + (float)Math.random() * 10 - 5, a.y + (float)Math.random() * 10 - 5);
                    b.speed = a.speed;
                    b.slip = a.slip;
                /*if(Math.random() < 0.5) {
                    if(Math.random() < 0.5) b.speed -= 0.1;
                    else b.speed += 0.1;
                    if(b.speed < 0.1f) b.speed = 0.1f;
                    else if(b.speed > 10f) b.speed = 10f;
                }*/
                bacteria.add(b);
            }
            if(a.food <= 0) {
                a.toBeDeleted = true;
            }
            else {
                if(a.age % 300 == 299) {
                    a.food -= 0.2f;
                }
                a.age++;
            }
            if(a.toBeDeleted) {
                bacteria.remove(i);
                i--;
            }
        }
        for (int i = 0; i < food.size(); i++) {
            if(food.get(i).toBeDeleted) {
                food.remove(i);
                i--;
            }
        }
        if(frame % 30 == 0) {
            Food a = new Food((float)(Math.random() * (w - 100) + 50), (float)(Math.random() * (h - 100) + 50));
            food.add(a);
        }
        frame++;
    }

}
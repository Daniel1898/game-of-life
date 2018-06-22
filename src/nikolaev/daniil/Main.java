package nikolaev.daniil;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.nio.file.*;
import java.util.List;


import edu.princeton.cs.introcs.StdDraw;

public class Main {
    public static final int MAX_WIDTH = 1200;
    public static final int MAX_HEIGHT = 680;
    public static final int MIN_WIDTH = -10;
    public static final int MIN_HEIGHT = -10;
    public static final int OFFSET = 10;

    double dx = 0;
    double dy = 0;
    double dr = 0.01;

    List<List<Point>> population = new ArrayList<>();
    double radius = 0.1;
    int fx = 0;
    int fy = 0;
    int pointCount;
    int genCount=0;

    class Point {
        int x;
        int y;

        Point(int x_, int y_) {
            x = x_;
            y = y_;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if ((o == null)
                    || (getClass() != o.getClass())) {
                return false;
            }
            Point point = (Point) o;
            return x == point.x &&
                    y == point.y;
        }

        @Override
        public int hashCode() {
            return y * 10000000 + x;

        }
    }

    /***
     * Установка параметров отрисовки
     */
    public void setCanvasSettings() {
        StdDraw.setCanvasSize(MAX_WIDTH, MAX_HEIGHT);
        StdDraw.setXscale(MIN_WIDTH, MAX_WIDTH);
        StdDraw.setYscale(MAX_HEIGHT, MIN_HEIGHT);
        StdDraw.enableDoubleBuffering();
        StdDraw.clear(Color.WHITE);
    }


    public void draw() {

    }

    public List<List<Point>> generate() {
        List<List<Point>> newGen=new ArrayList<>();

        return newGen;
    }

    /***
     * Считает живых соседей точки с координатами x y
     * @return количество живых соседей точки
     */
    public int neibhorCalc() {
        int sum=0;
        return sum;
    }

    /***
     * Обрабатывает строки считанные из файла
     * @param str
     */
    public void fileStringProcessing(String str) {
        int len = str.length();

        if ((len != 0) && ((str.charAt(0) == 'o')
                || (str.charAt(0) == 'b')
                || ((str.charAt(0) > '0') && (str.charAt(0) <= '9'))
                || (str.charAt(0) == '$'))) {

            for (int i = 0; i < len; i++) {
                if (str.charAt(i) == 'o') {
                    if (pointCount == 0) pointCount = 1;
                    for (int j = 0; j < pointCount; j++) {
                        Point point = new Point(fx, fy);
                        population.get(fy).add(point);
                        fx++;
                    }
                    pointCount = 0;
                } else if (str.charAt(i) == 'b') {
                    if (pointCount == 0) pointCount = 1;
                    for (int j = 0; j < pointCount; j++) {
                        fx++;
                    }
                    pointCount = 0;
                } else if (str.charAt(i) == '$') {
                    if (pointCount == 0) pointCount = 1;
                    fx = 0;
                    for (int j = 0; j < pointCount; j++) {
                        fy++;
                        population.add(new ArrayList<>());
                    }

                    pointCount = 0;
                } else {
                    pointCount = pointCount * 10 + str.charAt(i) - '0';
                }
            }
        }
    }

    /***
     * Считывает строки из файла
     * @throws IOException
     */
    public void fileRead() throws IOException {
        BufferedReader bReader =
                Files.newBufferedReader(Paths.get("/home/daniel/IdeaProjects/game of life/populations/caterpillar.rle"));
        fx = 0;
        fy = 0;
        int k = 0;
        pointCount = 0;
        String line;
        population.add(new ArrayList<>());
        while ((line = bReader.readLine()) != null) {
            k++;
            System.out.println(k);
            fileStringProcessing(line);
        }
        System.out.println("ready");
    }

    /***
     * Обрабатывает клавиши масштабирования
     * и передвижения по экрану
     */
    public void keys() {

        if (StdDraw.isKeyPressed(KeyEvent.VK_SUBTRACT)) {

            if ((radius - dr) <= dr) {
                dr *= 0.1;
            }
            radius -= dr;

        }
        if (StdDraw.isKeyPressed(KeyEvent.VK_ADD)) {
            if (((radius + dr) >= dr)
                    && (dr < 0.1)) {
                dr *= 10;
            }
            radius += dr;
        }
        if (StdDraw.isKeyPressed(KeyEvent.VK_LEFT)) {
            dx += OFFSET;
        }
        if (StdDraw.isKeyPressed(KeyEvent.VK_RIGHT)) {
            dx -= OFFSET;
        }
        if (StdDraw.isKeyPressed(KeyEvent.VK_UP)) {
            dy += OFFSET;
        }
        if (StdDraw.isKeyPressed(KeyEvent.VK_DOWN)) {
            dy -= OFFSET;
        }

    }

    public void start()
    {
        setCanvasSettings();
        try {
            fileRead();
            draw();
            StdDraw.show();


            while (true) {
                long tStart = System.currentTimeMillis();
                keys();

                population = generate();
                draw();
                long tFrame = System.currentTimeMillis() - tStart;
                String time = "frame:" + tFrame + "ms";
                String fps = "fps: " + (1000.0 / tFrame);
                String radiusStr = "radius:" + radius;
                String generation = "generation:" + genCount;
                StdDraw.setPenColor(Color.RED);
                StdDraw.textLeft(MIN_WIDTH + 20, MAX_HEIGHT - 10, time);
                StdDraw.textLeft(MIN_WIDTH + 20, MAX_HEIGHT - 30, fps);
                StdDraw.textLeft(MIN_WIDTH + 20, MAX_HEIGHT - 60, radiusStr);
                StdDraw.textLeft(MIN_WIDTH + 20, MAX_HEIGHT - 90, generation);
                StdDraw.show();
            }
        } catch (IOException e) {
            StdDraw.text(MAX_WIDTH / 2, MAX_HEIGHT / 2, "File doesn't exist");
            StdDraw.show();
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        Main app = new Main();
        app.start();

    }

}



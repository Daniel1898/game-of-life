package nikolaev.daniil;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.nio.file.*;


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

    Map<Point, Point> population = new HashMap<>();
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
            return x*1000000+y;
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
        StdDraw.clear(Color.white);
        StdDraw.setPenColor(Color.black);
        for (Point p:population.values()) {
            int x=p.x;
            int y=p.y;
            if ((2 * radius * (x + dx) >= MIN_WIDTH)
                    && (2 * radius * (x + dx) <= MAX_WIDTH)
                    && (2 * radius * (y + dy) >= MIN_HEIGHT)
                    && (2 * radius * (y + dy) <= MAX_HEIGHT)) {
                StdDraw.filledRectangle(2 * radius * (x + dx), 2 * radius * (y + dy),radius, radius);
            }
        }
    }

    /**
     * Генерация следующего поколения
     * @return Map в котором хранятся точки новой популяции
     */
    public Map<Point, Point> generate() {
        genCount++;
        System.out.println("generate " + population.size());
        int neibhors = 0;
        /*
         * map используется для запоминания состояния
         * текущей точки и ее соседей
         * если точка "жива" хранит 1 если нет хранит 0
         * если состояние не известно хранит -1
         */
        Integer[][] map=new Integer[5][5];
        Map<Point, Point> newGen = new HashMap<>();
        for (Point p : population.values()) {
            //Очистка map
            for (int i=0;i<5;i++)
            {
                for (int j=0;j<5;j++)
                {
                    map[i][j]=-1;
                }
            }

            neibhors = neibhorCalc(p.y, p.x, map, 1, 1);
            if ((neibhors == 2) || (neibhors == 3)) {
                newGen.put(p, p);
            }
            checkNeibhors(p.x, p.y, newGen, map);
        }

        return newGen;
    }

    /***
     * Проверяет соседей точки с координатами x и y
     * @param x координата x
     * @param y координата y
     * @param gen map хранящий новую популяцию
     * @param nebhorMap массив с состояниями точку с координатами х у и ее соседей
     */
    void checkNeibhors(int x, int y, Map<Point, Point> gen, Integer[][] nebhorMap) {
        Point p;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (!(i == 0 && j == 0)) {
                    p = new Point(x + i, y + j);
                    if (nebhorMap[i + 2][j + 2] <= 0) {
                        if (neibhorCalc(p.y, p.x, nebhorMap, i + 1, j + 1) == 3) {
                            gen.put(p, p);
                        }
                    }
                }
            }
        }
    }

    /***
     * Считает живых соседей точки с координатами x y
     * @param y координата y передаваемой точки
     * @param x координата х передаваемой точки
     * @param nebhorMap массив с состояниями точки с координатами х у и ее соседей
     * @param dy
     * @param dx
     * @return количество живых соседей точки
     */
    public int neibhorCalc(int y, int x, Integer[][] nebhorMap, int dy, int dx) {
        int sum = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                /*
                 * Проверяем известно ли состояние точки.
                 * Если известно берем состояние из массива
                 * иначе проверяем есть ли жива ли точка в текущем околении
                 */
                if (nebhorMap[i + 1 + dy][j + 1 + dx] == -1) {
                    if (((i|j)!=0)
                            && (population.containsKey(new Point(x + i, y + j)))) {
                        nebhorMap[i + 1 + dy][j + 1 + dx] = 1;
                        sum++;
                    } else {
                        if ((i|j)!=0) {
                            nebhorMap[i + 1 + dy][j + 1 + dx] = 0;
                        }
                    }
                } else {
                    if ((i|j)!=0) {
                        sum += nebhorMap[i + 1 + dy][j + 1 + dx];
                    }
                }
            }
        }
        return sum;
    }

    /***
     * Обрабатывает строки считанные из файла
     * @param str
     */
    public void fileStringProcessing(String str) {
        int len = str.length();

        if ((len!=0) && ((str.charAt(0) == 'o')
                || (str.charAt(0) == 'b')
                || ((str.charAt(0) > '0') && (str.charAt(0) <= '9'))
                || (str.charAt(0) == '$'))) {

            for (int i = 0; i < len; i++) {
                if (str.charAt(i) == 'o') {
                    if (pointCount == 0) pointCount = 1;
                    for (int j = 0; j < pointCount; j++) {
                        Point point = new Point(fx, fy);
                        population.put(point, point);
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
                Files.newBufferedReader(Paths.get("/home/daniel/IdeaProjects/game of life/populations/lineship1.rle"));
        fx = 0;
        fy = 0;
        int k = 0;
        pointCount = 0;
        String line;
        while ((line = bReader.readLine()) != null) {
            k++;
            System.out.println(k);
            fileStringProcessing(line);
        }
        System.out.println("ready");
    }

    /***
     * Обрабатывает клавиши масштабирования
     * и передвижения о экрану
     */
    public void keys() {

        if (StdDraw.isKeyPressed(KeyEvent.VK_SUBTRACT)) {

            if (radius - dr <= dr) {
                dr *= 0.1;
            }
            radius -= dr;

        }
        if (StdDraw.isKeyPressed(KeyEvent.VK_ADD)) {
            if ((radius + dr >= dr)
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

    public static void main(String[] args) throws InterruptedException {

        Main app = new Main();
        app.setCanvasSettings();
        try {
            app.fileRead();
            app.draw();
            StdDraw.show();


            while (true) {
                long tStart = System.currentTimeMillis();
                app.keys();

                app.population = app.generate();
                app.draw();
                long tFrame = System.currentTimeMillis() - tStart;
                String time = "frame:" + tFrame + "ms";
                String fps = "fps: " + (1000.0 / tFrame);
                String radius = "radius:" + app.radius;
                String generation = "generation:" + app.genCount;
                StdDraw.setPenColor(Color.RED);
                StdDraw.textLeft(MIN_WIDTH + 20, MAX_HEIGHT - 10, time);
                StdDraw.textLeft(MIN_WIDTH + 20, MAX_HEIGHT - 30, fps);
                StdDraw.textLeft(MIN_WIDTH + 20, MAX_HEIGHT - 60, radius);
                StdDraw.textLeft(MIN_WIDTH + 20, MAX_HEIGHT - 90, generation);
                StdDraw.show();
            }
        } catch (IOException e) {
            StdDraw.text(MAX_WIDTH / 2, MAX_HEIGHT / 2, "File doesn't exist");
            StdDraw.show();
            e.printStackTrace();
        }

    }

}



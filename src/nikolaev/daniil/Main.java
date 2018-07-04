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

    List<PointRow> population = new ArrayList<>();
    double radius = 0.001;
    int fx = 0;
    int fy = 0;
    int pointCount;
    int genCount = 0;


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

    public class PointRow extends ArrayList<Integer> implements Comparable<Integer> {
        int index;

        PointRow(int index) {
            super();
            this.index = index;
        }

        @Override
        public int compareTo(Integer integer) {
            return integer.compareTo(index);
        }
    }


    public void draw() {
        StdDraw.clear(Color.white);
        StdDraw.setPenColor(Color.black);
        for (PointRow y : population) {
            if ((2 * radius * (y.index + dy) >= MIN_HEIGHT)
                    && (2 * radius * (y.index + dy) <= MAX_HEIGHT))
                for (int x : y) {
                    if ((2 * radius * (x + dx) >= MIN_WIDTH)
                            && (2 * radius * (x + dx) <= MAX_WIDTH)) {
                        StdDraw.filledRectangle(2 * radius * (x + dx), 2 * radius * (y.index + dy), radius, radius);
                    }
                }
        }
    }

    public List<PointRow> generate() {
        genCount++;
        List<PointRow> newGen = new ArrayList<>();
        PointRow ly = null;
        PointRow cy = null;
        PointRow ry = null;
        PointRow next = null;
        int index = population.get(0).index - 1;
        int lastLine = 2;

        ListIterator<PointRow> rowIterator = population.listIterator();
        while (rowIterator.hasNext() || lastLine != 0) {

            ly = cy;
            cy = ry;
            if (rowIterator.hasNext()) {
                ry = rowIterator.next();

                if (cy != null) {
                    if (cy.index + 1 != ry.index) {
                        ry = null;
                        rowIterator.previous();
                    }

                } else {
                    if (ly != null) {
                        if (ly.index + 2 != ry.index) {
                            ry = null;
                            rowIterator.previous();
                        }
                    }
                }
            } else {
                ry = null;
                lastLine--;
            }
            PointRow r = genNewRow(index, ly, cy, ry);
            if (r.size() > 0) {
                newGen.add(genNewRow(index, ly, cy, ry));
            }
            index++;
        }
        return newGen;
    }

    public PointRow genNewRow(int index, PointRow up, PointRow c, PointRow down) {
        PointRow newRow = new PointRow(index);
        Set<Integer> newPoints = new TreeSet<>();
        Iterator<Integer> upIt = (up != null) ? up.iterator() : new Iterator<Integer>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public Integer next() {
                return 0;
            }
        };
        Iterator<Integer> cIt = (c != null) ? c.iterator() : new Iterator<Integer>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public Integer next() {
                return 0;
            }
        };
        Iterator<Integer> downIt = (down != null) ? down.iterator() : new Iterator<Integer>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public Integer next() {
                return 0;
            }
        };
        int[] points = new int[3];
        while (upIt.hasNext() || cIt.hasNext() || downIt.hasNext()) {
            if (upIt.hasNext()) {
                points[0] = upIt.next();
            } else {
                points[0] = -999999999;
            }
            if (cIt.hasNext()) {
                points[1] = cIt.next();
            } else {
                points[1] = -999999999;
            }
            if (downIt.hasNext()) {
                points[2] = downIt.next();
            } else {
                points[2] = -999999999;
            }
            Arrays.sort(points);
            for (int i = 0; i < 3; i++) {
                if ((i != 0 && points[i] == points[i - 1]) || points[i] == -999999999) {
                    continue;
                }
                if (!newPoints.contains(points[i] - 1)) {
                    if (checkPoint(points[i] - 1, up, c, down)) {
                        newPoints.add(points[i] - 1);
                    }
                }
                if (checkPoint(points[i], up, c, down)) {
                    newPoints.add(points[i]);
                }
                if (!newPoints.contains(points[i] + 1)) {
                    if (checkPoint(points[i] + 1, up, c, down)) {
                        newPoints.add(points[i] + 1);
                    }
                }
            }
        }
        newRow.addAll(newPoints);
        return newRow;
    }

    public boolean checkPoint(int x, PointRow up, PointRow c, PointRow down) {
        int neibhors = checkNeibhors(x, up, c, down);
        if (c != null && Collections.binarySearch(c, x) >= 0) {
            if (neibhors == 3 || neibhors == 2) {
                return true;
            } else {
                return false;
            }
        } else {
            if (neibhors == 3) {
                return true;
            } else {
                return false;
            }
        }

    }

    public int checkNeibhors(int x, PointRow up, PointRow c, PointRow down) {
        int sum = 0;
        sum+=rowSum(up,x);
        if (c != null) {
            sum += (Collections.binarySearch(c, x + 1) >= 0) ? 1 : 0;
            sum += (Collections.binarySearch(c, x - 1) >= 0) ? 1 : 0;
        }
        sum+=rowSum(down,x);
        return sum;
    }

    public int rowSum(PointRow row,int x) {
        int sum=0;
        int index;
        if (row != null) {
            index = Collections.binarySearch(row, x);
            if (index >= 0) {
                if (index < row.size() - 1) {
                    sum += (row.get(index + 1) - 1 == x) ? 1 : 0;
                }
                sum++;
                if (index != 0) {
                    sum += (row.get(index - 1) + 1 == x) ? 1 : 0;
                }
            } else {
                index=Collections.binarySearch(row, x + 1);
                if (index>=0)
                {
                    sum++;
                    if (index != 0) {
                        sum += (row.get(index - 1) + 2 == row.get(index)) ? 1 : 0;
                    }
                } else
                {
                    sum += (Collections.binarySearch(row, x - 1) >= 0) ? 1 : 0;
                }
            }
        }
        return sum;
    }
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
                        population.get(fy).add(fx);
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
                        population.add(new PointRow(fy));
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
        population.add(new PointRow(0));
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

    public void start() {
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



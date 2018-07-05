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
    public static final int INFINITY= -999999999;


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
     * Setting drawing options
     */
    public void setCanvasOptions() {
        StdDraw.setCanvasSize(MAX_WIDTH, MAX_HEIGHT);
        StdDraw.setXscale(MIN_WIDTH, MAX_WIDTH);
        StdDraw.setYscale(MAX_HEIGHT, MIN_HEIGHT);
        StdDraw.enableDoubleBuffering();
        StdDraw.clear(Color.WHITE);
    }

    /***
     * Container to storing the coordinate of a points
     */
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
            if (((2 * radius * (y.index + dy)) >= MIN_HEIGHT)
                    && ((2 * radius * (y.index + dy)) <= MAX_HEIGHT))
                for (int x : y) {
                    if (((2 * radius * (x + dx)) >= MIN_WIDTH)
                            && ((2 * radius * (x + dx)) <= MAX_WIDTH)) {
                        StdDraw.filledRectangle(2 * radius * (x + dx), 2 * radius * (y.index + dy), radius, radius);
                    }
                }
        }
    }

    /***
     * Generate new generation of points
     * @return list of PointRow
     */
    public List<PointRow> generate() {
        genCount++;
        List<PointRow> newGen = new ArrayList<>();
        PointRow upperRow = null;
        PointRow centerRow = null;
        PointRow lowerRow = null;

        int index = (population.isEmpty()) ? 0 : population.get(0).index - 1;
        int lastLine = 2;

        ListIterator<PointRow> rowIterator = population.listIterator();
        while ((rowIterator.hasNext()) || (lastLine != 0)) {
            upperRow = centerRow;
            centerRow = lowerRow;
            if (rowIterator.hasNext()) {
                lowerRow = rowIterator.next();
                if (centerRow != null) {
                    if ((centerRow.index + 1) != lowerRow.index) {
                        lowerRow = null;
                        rowIterator.previous();
                    }
                } else {
                    if (upperRow != null) {
                        if ((upperRow.index + 2) != lowerRow.index) {
                            lowerRow = null;
                            rowIterator.previous();
                        }
                    } else {
                        index = lowerRow.index - 1;
                    }
                }
            } else {
                lowerRow = null;
                lastLine--;
            }
            PointRow newRow = genNewRow(index, upperRow, centerRow, lowerRow);
            if (newRow.size() > 0) {
                newGen.add(newRow);
            }
            index++;
        }
        return newGen;
    }

    /***
     * Generate new row this points
     * @param index row number
     * @param upperRow
     * @param centerRow
     * @param lowerRow
     * @return generated PointRow
     */
    public PointRow genNewRow(int index, PointRow upperRow, PointRow centerRow, PointRow lowerRow) {
        PointRow newRow = new PointRow(index);
        Set<Integer> newPoints = new TreeSet<>();
        int[] points = new int[3];
        int[][] pointMap;
        Iterator<Integer> upperRowIt = (upperRow != null) ? upperRow.iterator() : new Iterator<Integer>() {
            @Override
            public boolean hasNext() {

                return false;
            }

            @Override
            public Integer next() {

                return 0;
            }
        };
        Iterator<Integer> centerRowIt = (centerRow != null) ? centerRow.iterator() : new Iterator<Integer>() {
            @Override
            public boolean hasNext() {

                return false;
            }

            @Override
            public Integer next() {

                return 0;
            }
        };
        Iterator<Integer> lowerRowIt = (lowerRow != null) ? lowerRow.iterator() : new Iterator<Integer>() {
            @Override
            public boolean hasNext() {

                return false;
            }

            @Override
            public Integer next() {

                return 0;
            }
        };


        while (upperRowIt.hasNext() || centerRowIt.hasNext() || lowerRowIt.hasNext()) {
            if (upperRowIt.hasNext()) {
                points[0] = upperRowIt.next();
            } else {
                points[0] = INFINITY;
            }
            if (centerRowIt.hasNext()) {
                points[1] = centerRowIt.next();
            } else {
                points[1] = INFINITY;
            }
            if (lowerRowIt.hasNext()) {
                points[2] = lowerRowIt.next();
            } else {
                points[2] = INFINITY;
            }

            Arrays.sort(points);

            for (int i = 0; i < 3; i++) {
                if (((i != 0) && (points[i] == points[i - 1]))
                        || (points[i] == INFINITY))
                {
                    continue;
                }
                pointMap = generatePointMap(points[i],upperRow,centerRow,lowerRow);
                if (!newPoints.contains(points[i] - 1)) {
                    if (checkPoint(-1, pointMap)) {
                        newPoints.add(points[i] - 1);
                    }
                }
                if (checkPoint(0, pointMap)) {
                    newPoints.add(points[i]);
                }
                if (!newPoints.contains(points[i] + 1)) {
                    if (checkPoint(1, pointMap)) {
                        newPoints.add(points[i] + 1);
                    }
                }
            }
        }
        newRow.addAll(newPoints);
        return newRow;
    }

    /***
     * Generate array with point status (alive or dead)
     * around point with coordinate x
     * @param x
     * @param upperRow
     * @param centerRow
     * @param lowerRow
     * @return point status array
     */
    public int[][] generatePointMap(int x, PointRow upperRow, PointRow centerRow, PointRow lowerRow)
    {
        int[][] map = new int[3][5];
        rowMap(upperRow, map, x,0);
        rowMap(centerRow, map, x,1);
        rowMap(lowerRow, map, x,2);
        return map;
    }

    /***
     * Fill point map row
     * @param map point status array
     * @param i row number
     */
    public void rowMap(PointRow row, int[][] map, int x, int i)
    {
        if (row!=null) {
            int index = Collections.binarySearch(row, x - 2);
            if (index < 0) {
                index = index * -1 - 1;
            } else {
                map[i][0] = 1;
            }
            while ((index <= row.size()-1) && (row.get(index) <= x + 2)) {

                if (row.get(index) == x - 1) {
                    map[i][1] = 1;
                } else {
                    if (row.get(index) == x) {
                        map[i][2] = 1;
                    } else {
                        if (row.get(index) == x + 1) {
                            map[i][3] = 1;
                        } else {
                            if (row.get(index) == x + 2) {
                                map[i][4] = 1;
                            }
                        }
                    }
                }
                index++;
            }
        }
    }

    /***
     * Checks if the cell is alive in the next generation
     * @return if cell is alive true else false
     */
    public boolean checkPoint(int dc, int[][] pointMap)
    {
        int index = 2 + dc;
        int neighbors = checkNeighbors(pointMap, index);

        if (pointMap[1][index] == 1) {
            if ((neighbors == 3)
                    || (neighbors == 2)) {
                return true;
            } else {
                return false;
            }
        } else {
            if (neighbors == 3) {
                return true;
            } else {
                return false;
            }
        }
    }

    public int checkNeighbors(int [][] pointMap, int index) {
        int sum = 0;
        sum += pointMap[0][index - 1]
                + pointMap[0][index]
                + pointMap[0][index + 1]
                + pointMap[1][index - 1]
                + pointMap[1][index + 1]
                + pointMap[2][index - 1]
                + pointMap[2][index]
                + pointMap[2][index + 1];
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
     * Read strings from file
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
        setCanvasOptions();
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



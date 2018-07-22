package nikolaev.daniil;

import edu.princeton.cs.introcs.StdDraw;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


public class Main {
    public static final int MAX_WIDTH = 1200;
    public static final int MAX_HEIGHT = 680;
    public static final int MIN_WIDTH = -10;
    public static final int MIN_HEIGHT = -10;
    public static final int OFFSET = 10;
    public static final int INFINITY = 999999999;


    double dx = 0;
    double dy = 0;
    double dr = 0.01;


    List<PointRow> population = new ArrayList<>();

    double radius = 0.01;
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
    public class PointRow extends IntArrayList {
        int index;

        PointRow(int index) {
            super();
            this.index = index;
        }
    }


    public void draw() {
        boolean ff;
        StdDraw.clear(Color.white);
        StdDraw.setPenColor(Color.black);
        for (PointRow y : population) {
            if (((2 * radius * (y.index + dy)) >= MIN_HEIGHT)
                    && ((2 * radius * (y.index + dy)) <= MAX_HEIGHT)) {
                ff = false;
                for (int x:y)
                {
                    if (radius > 0.25 || !ff) {
                        if (((2 * radius * (x + dx)) >= MIN_WIDTH)
                                && ((2 * radius * (x + dx)) <= MAX_WIDTH)) {
                            StdDraw.filledSquare(2 * radius * (x + dx), 2 * radius * (y.index + dy), radius);
                            ff = true;
                        }
                        else
                        {
                            ff = false;
                        }
                    } else
                    {
                        ff = false;
                    }
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
                if (((upperRow != null) && ((upperRow.index + 2) != lowerRow.index))
                        || ((centerRow != null) && ((centerRow.index + 1) != lowerRow.index))) {
                    lowerRow = null;
                    rowIterator.previous();
                } else {
                    index = lowerRow.index - 1;
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
     * Generate new row with points
     * @param index row number
     * @param upperRow row above processing row
     * @param centerRow processing row
     * @param lowerRow row under processing row
     * @return generated PointRow
     */
    public PointRow genNewRow(int index,
                              PointRow upperRow,
                              PointRow centerRow,
                              PointRow lowerRow) {

        PointRow newRow = new PointRow(index);
        byte[] pointMap = new byte[3];
        int prevX;
        int x = 0;
        int[] prevIndex = new int[3];
        boolean firstIter = true;
        int[] prev= new int[] {INFINITY, INFINITY};

        IntIterator p = mergeRows(upperRow, centerRow, lowerRow);

        while (p.hasNext()) {
            if (firstIter)
            {
                prevX = p.nextInt();
                x = prevX;
                firstIter = false;
            }
            else
            {
                prevX = x;
                x = p.nextInt();
            }
            pointMap = generatePointMap(x, prevX, prevIndex, upperRow, centerRow, lowerRow, pointMap);
            for (int i = -1; i <= 1; i++) {
                if ((prev[0]!= (x + i)) && (prev[1]!= (x + i))) {
                    addPointToRow(x, i, newRow, pointMap);
                    prev[0]=prev[1];
                    prev[1]= x + i;
                }
            }
        }

        return newRow;
    }

    private IntIterator mergeRows(PointRow upperRow, PointRow centerRow, PointRow lowerRow) {
        IntArrayList rez = new IntArrayList();
        IntIterator uIt = (upperRow != null)? upperRow.iterator() : IntIterators.EMPTY_ITERATOR;
        IntIterator cIt = (centerRow != null)? centerRow.iterator() : IntIterators.EMPTY_ITERATOR;
        IntIterator lIt = (lowerRow != null)? lowerRow.iterator() : IntIterators.EMPTY_ITERATOR;
        int n1 = (uIt.hasNext())? uIt.nextInt() : INFINITY;
        int n2 = (cIt.hasNext())? cIt.nextInt() : INFINITY;
        int n3 = (lIt.hasNext())? lIt.nextInt() : INFINITY;
        int min = Math.min(Math.min(n1, n2), n3);;
        int last = INFINITY;
        while (min != INFINITY)
        {
            if (last != min)
            {
                rez.add(min);
                last = min;
            }
            if (n1 == min)
            {
                n1 = (uIt.hasNext())? uIt.nextInt() : INFINITY;
            }
            if (n2 == min)
            {
                n2 = (cIt.hasNext())? cIt.nextInt() : INFINITY;
            }
            if (n3 == min)
            {
                n3 = (lIt.hasNext())? lIt.nextInt() : INFINITY;
            }
            min = Math.min(Math.min(n1, n2), n3);
        }
        return rez.iterator();
    }


    /**
     * Adding point to row with coordinate x + dx
     * @param x
     * @param dx deviation from x
     * @param newRow row to adding points
     * @param pointMap
     */
    public void addPointToRow(int x,
                              int dx,
                              IntArrayList newRow,
                              byte[] pointMap)
    {
            if (checkPoint(dx, pointMap)) {
                newRow.add(x + dx);
            }
    }


    /***
     * Generate array with statuses (alive or dead) of point with
     * coordinate x and her neighbors
     * @param x coordinate of central point
     * @param upperRow row above processing row
     * @param centerRow processing row
     * @param lowerRow row under processing row
     * @return point status array
     */
    public byte[] generatePointMap(int x,
                                    int prevX,
                                    int[] prevIndex,
                                    PointRow upperRow,
                                    PointRow centerRow,
                                    PointRow lowerRow,
                                    byte[] map)
    {


        int index = 0;
        if ((x - prevX > 0) && (x - prevX < 5)) {
            int k = x - prevX;
            for (int i = 0; i < 3; i++) {
                map[i] = (byte) ((map[i] << k) & 0b11111);
            }
            index = 5 - (x - prevX);
        } else
        {
            for (int i = 0; i < 3; i++) {
                map[i] = 0;
            }
        }

        prevIndex[0] = rowMap(upperRow, map, 0, x, index, prevIndex[0]);
        prevIndex[1] = rowMap(centerRow, map ,1, x, index, prevIndex[1]);
        prevIndex[2] = rowMap(lowerRow, map, 2, x, index, prevIndex[2]);

        return map;
    }

    /**
     * Fill point map row
     * @param row row with points
     * @param map point map row
     * @param x coordinate x of central point (point with index 2)
     * @param j number of points with known state
     * @param prevIndex start index of searching alive points in row
     * @return index of last checked point +1
     */
    public int rowMap(PointRow row, byte[] map, int i, int x, int j, int prevIndex) {
        int index;
        if (row != null) {
            index = prevIndex;
            int r;
            while (index<row.size()) {
                r = Math.abs(row.getInt(index) - x) + 2;
                if (r >= j && r < 5)
                {
                   map[i] |= 16 >> r;
                }
                if (r < 5)
                {
                    index++;
                } else
                {
                    break;
                }
            }
            return index;
        }
        return prevIndex;
    }

    /***
     * Checks if the cell is alive in the next generation
     * @param dx deviation from center point map row
     * @param pointMap array with statuses (alive or dead) of point with
     * coordinate x and her neighbors
     * @return if cell is alive true else false
     */
    public boolean checkPoint(int dx, byte[] pointMap)
    {
        int index = 2 + dx;
        int neighbors = checkNeighbors(pointMap, index);

        if ((pointMap[1] & (16 >> index)) > 0) {
            return (neighbors == 3)
                    || (neighbors == 2);
        } else {
            return neighbors == 3;
        }
    }

    /***
     *  Calculating sum of neighbors of point with coordinate x
     * @param pointMap array with statuses (alive or dead) of point with
     * coordinate x and her neighbors
     * @param x coordinate of point
     * @return sum of neighbors of point with coordinate x
     */
    public int checkNeighbors(byte[] pointMap, int x) {
        int sum = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = -1; j <= 1; j++) {
                if (((i != 1) || (j != 0)) && ((pointMap[i] & (16 >> (x + j))) > 0)) {
                    sum += 1;
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

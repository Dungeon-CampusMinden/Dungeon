package level.generator.perlinNoise;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import level.tools.Coordinate;

/** area providing some methods to get areas from perlin noise */
public class NoiseArea {
    private int width;
    private int height;
    private int size;
    private boolean[][] area;
    /**
     * generates a new area
     *
     * @param contains two dimensional boolean array containing true for every field in the area
     */
    public NoiseArea(final boolean[][] contains) {
        width = contains.length;
        height = contains[0].length;
        area = contains;

        int i = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (area[x][y]) i++;
            }
        }
        size = i;
    }
    /**
     * checks whether the given coordinates are in the area
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return whether the given coordinates are in the area
     */
    public boolean contains(final int x, final int y) {
        return area[x][y];
    }

    /**
     * zoom the area
     *
     * @param zoom zoom factor
     */
    public void zoom(final double zoom) {
        width = (int) (width / zoom);
        height = (int) (height / zoom);
        size = (int) (getSize() / zoom);
        final boolean[][] newArea = new boolean[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                newArea[i][j] = area[(int) (i * zoom)][(int) (j * zoom)];
            }
        }
        area = newArea;
    }

    /**
     * returns the size of the area
     *
     * @return the size of the area
     */
    public int getSize() {
        return size;
    }

    /**
     * returns the width of the area
     *
     * @return the width of the area
     */
    public int getWidth() {
        return width;
    }

    /**
     * returns the height of the area
     *
     * @return the height of the area
     */
    public int getHeight() {
        return height;
    }

    /**
     * returns a buffered image representing the area
     *
     * <p>a white pixel represents a accessible area and a black one a wall
     *
     * @return a buffered image representing the area
     */
    public BufferedImage getImage() {
        final BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = res.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width - 1, height - 1);

        g.setColor(Color.WHITE);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (area[i][j]) {
                    g.fillRect(i, j, 1, 1);
                }
            }
        }
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, width - 1, height - 1);
        g.dispose();
        return res;
    }

    /**
     * generates areas from perlin noise
     *
     * @param values the NoiseAreaValues
     * @return all found areas
     */
    public static NoiseArea[] getAreas(final NoiseAreaValues values) {
        final ArrayList<NoiseArea> alRes = new ArrayList<>();
        for (int x = 0; x < values.noiseValues.length; x++) {
            allPixel:
            for (int y = 0; y < values.noiseValues[x].length; y++) {
                for (final NoiseArea f : alRes) {
                    if (f.contains(x, y)) continue allPixel;
                }
                if (checkBound(values.noiseValues[x][y], values)) {
                    final boolean[][] isContained =
                            floodFill(
                                    new NoiseAreaValues(
                                            values.min,
                                            values.max,
                                            values.noiseValues,
                                            new Coordinate(x, y),
                                            values.outerBound));
                    alRes.add(new NoiseArea(isContained));
                }
            }
        }
        return alRes.toArray(new NoiseArea[0]);
    }

    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    private static boolean[][] floodFill(final NoiseAreaValues values) {
        final boolean[][] res =
                new boolean[values.noiseValues.length][values.noiseValues[0].length];
        final ArrayList<Coordinate> queue = new ArrayList<>();
        queue.add(values.startField);

        while (!queue.isEmpty()) {
            final Coordinate aktFeld = queue.remove(0);
            final int x = aktFeld.x;
            final int y = aktFeld.y;

            if (checkBound(values.noiseValues[x][y], values) && !res[x][y]) {
                res[x][y] = true;

                if (x > 0) queue.add(new Coordinate(x - 1, y));
                if (x < values.noiseValues.length - 1) queue.add(new Coordinate(x + 1, y));
                if (y > 0) queue.add(new Coordinate(x, y - 1));
                if (y < values.noiseValues[x].length - 1) queue.add(new Coordinate(x, y + 1));
            }
        }
        return res;
    }

    private static boolean checkBound(final double value, final NoiseAreaValues values) {
        if (values.outerBound) {
            return (value <= values.min || value >= values.max);
        }
        return (value >= values.min && value <= values.max);
    }
}

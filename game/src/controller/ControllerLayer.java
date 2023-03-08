package controller;

/**
 * A class to represent a layer on wich elements are drawn.
 *
 * <p>BOTTOM: 100 (you can de- or increase this value if you need to) <br>
 * DEFAULT: 200 (you can de- or increase this value if you need to) <br>
 * TOP: 300 (you can de- or increase this value if you need to)
 *
 * <p>ControllerLayers with a higher value are drawn last (i.e. over other entities).
 *
 * <p>You can construct a user-defined ControllerLayer with any possible integer value.
 */
public record ControllerLayer(int value) implements Comparable<ControllerLayer> {
    public static final int BOTTOM_VALUE = 100;
    public static final int DEFAULT_VALUE = 200;
    public static final int TOP_VALUE = 300;
    public static final ControllerLayer BOTTOM = new ControllerLayer(BOTTOM_VALUE);
    public static final ControllerLayer DEFAULT = new ControllerLayer(DEFAULT_VALUE);
    public static final ControllerLayer TOP = new ControllerLayer(TOP_VALUE);

    @Override
    public int compareTo(ControllerLayer o) {
        return Integer.compare(value, o.value);
    }
}

package tools;

import java.net.URL;

public final class Constants {
    public static final int WINDOW_WIDTH = 640;

    public static final int WINDOW_HEIGHT = 480;

    /** Frames per seconds. */
    public static final int FRAME_RATE = 30;

    /** Virtual width and height. */
    public static final float FIELD_WIDTH_AND_HEIGHT_IN_PIXEL = 16f;

    public static final float VIRTUAL_WIDTH = WINDOW_WIDTH / FIELD_WIDTH_AND_HEIGHT_IN_PIXEL;
    public static final float VIRTUAL_HEIGHT = WINDOW_HEIGHT / FIELD_WIDTH_AND_HEIGHT_IN_PIXEL;

    /** 200% zoom. */
    public static final float DEFAULT_ZOOM_FACTOR = 0.5f;
    /** Disable Replacements in DungeonG. */
    public static final boolean DISABLE_REPLACEMENTS = true;

    /**
     * @param path the relative path to the resource
     * @return the absolute path of the internal resource
     */
    private static String getResourceString(String path) {
        URL url = ClassLoader.getSystemClassLoader().getResource(path);
        assert (url != null);
        return url.getPath();
    }

    /**
     * Returns the absolute path to the resource inside the "assets" folder. The location of the
     * "assets" folder is determined dynamically.
     *
     * @return the absolute path to the resource inside the "assets" folder.
     */
    public static String getPathToLevel() {
        return getResourceString("level/files");
    }

    /**
     * Returns the absolute path to the resource inside the "assets" folder. The location of the
     * "assets" folder is determined dynamically.
     *
     * @return the absolute path to the resource inside the "assets" folder.
     */
    public static String getPathToGraph() {
        return getResourceString("level/graphs");
    }

    /**
     * Returns the absolute path to the resource inside the "assets" folder. The location of the
     * "assets" folder is determined dynamically.
     *
     * @return the absolute path to the resource inside the "assets" folder.
     */
    public static String getPathToRoomTemplates() {
        return getResourceString("level/roomTemplates.json");
    }

    /**
     * Returns the absolute path to the resource inside the "assets" folder. The location of the
     * "assets" folder is determined dynamically.
     *
     * @return the absolute path to the resource inside the "assets" folder.
     */
    public static String getPathToReplacements() {
        return getResourceString("level/replacements.json");
    }
}

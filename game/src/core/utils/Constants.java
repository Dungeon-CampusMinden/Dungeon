package core.utils;

import core.Game;

import java.net.URISyntaxException;
import java.net.URL;

public final class Constants {

    /** Virtual width and height. */
    public static final float FIELD_WIDTH_AND_HEIGHT_IN_PIXEL = 16f;

    public static final float VIEWPORT_WIDTH = Game.WINDOW_WIDTH / FIELD_WIDTH_AND_HEIGHT_IN_PIXEL;
    public static final float VIEWPORT_HEIGHT =
            Game.WINDOW_HEIGHT / FIELD_WIDTH_AND_HEIGHT_IN_PIXEL;

    /** 200% zoom. */
    public static final float DEFAULT_ZOOM_FACTOR = 0.5f;

    /** Value for LevelElements that are accessible */
    public static final boolean LEVELELEMENT_IS_ACCESSIBLE = true;
    /** Value for LevelElements that are not accessible */
    public static final boolean LEVELELEMENT_IS_NOT_ACCESSIBLE = false;

    /** set Path to libgdx default Skins */
    public static final String SKIN_FOR_DIALOG = "skin/uiskin.json";

    public static final int DIALOG_DIFFERENCE_MEASURE = 70;
    public static final String DEFAULT_HEADING = "Default heading";
    public static final String DEFAULT_MESSAGE = "Das Spiel ist pausiert.";
    public static final String DEFAULT_BUTTON_MESSAGE = "OK ";
    public static final String QUIZ_MESSAGE_TASK = "Aufgabestellung";

    public static final String QUIZ_MESSAGE_SOLUTION = "Lösung";

    public static final String TEST_IMAGE_PATH_FOR_DIALOG = "image_quiz/dummy.png";

    /**
     * @param path the relative path to the resource
     * @return the absolute path of the internal resource
     */
    @SuppressWarnings("unused")
    private static String getResourceString(String path) {
        URL url = ClassLoader.getSystemClassLoader().getResource(path);
        assert (url != null);
        String modifiedPath = null;
        try {
            modifiedPath = url.toURI().getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        assert (modifiedPath != null);
        return modifiedPath;
    }
}

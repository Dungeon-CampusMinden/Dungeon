package core.utils.components.draw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.utils.GdxNativesLoader;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class TextureHandlerTest {
    private static final String PLACEHOLDER_MIDDLE_PATH = "resources/main";

    @BeforeClass
    public static void setUpGdx() {
        GdxNativesLoader.load(); // load natives for headless testing
        Gdx.files = new Lwjgl3Files();
    }

    @Test
    public void test_getInstance() {
        TextureHandler instance = null;
        try {
            instance = TextureHandler.getInstance();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(instance);
    }

    @Test
    public void test_getTexturePaths_all() {
        TextureHandler instance = null;
        try {
            instance = TextureHandler.getInstance();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(instance);
        List<String> paths = instance.getTexturePaths("");
        Assert.assertNotNull(paths);
        Assert.assertFalse(paths.isEmpty());
        for (String path : paths) {
            Assert.assertTrue(path.contains(PLACEHOLDER_MIDDLE_PATH));
        }
    }

    @Test
    public void test_getTexturePaths_placeholder() {
        TextureHandler instance = null;
        try {
            instance = TextureHandler.getInstance();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(instance);
        List<String> paths = instance.getTexturePaths(TextureHandler.PLACEHOLDER_FILENAME);
        Assert.assertNotNull(paths);
        Assert.assertEquals(1, paths.size());
        Assert.assertTrue(
                paths.get(0)
                        .endsWith(
                                PLACEHOLDER_MIDDLE_PATH
                                        + "/"
                                        + TextureHandler.PLACEHOLDER_FILENAME));
    }
}

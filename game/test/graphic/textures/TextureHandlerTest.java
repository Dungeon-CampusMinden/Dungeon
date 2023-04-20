package graphic.textures;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.utils.GdxNativesLoader;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TextureHandlerTest {
    @BeforeClass
    public static void setUpGdx() {
        GdxNativesLoader.load(); // load natives for headless testing
        Gdx.files = new Lwjgl3Files();
    }

    @Test
    public void test_getInstance() {
        TextureHandler instance = TextureHandler.getInstance();
        Assert.assertNotNull(instance);
    }

    @Test
    public void test_getAvailablePaths() {
        TextureHandler instance = TextureHandler.getInstance();
        Assert.assertNotNull(instance);
        Set<String> paths = instance.getAvailablePaths();
        Assert.assertNotNull(paths);
        Assert.assertFalse(paths.isEmpty());
        for (String path : paths) {
            Assert.assertTrue(path.contains("build/resources/main"));
        }
    }

    @Test
    public void test_getTexturePaths() {
        TextureHandler instance = TextureHandler.getInstance();
        Assert.assertNotNull(instance);
        List<String> paths = instance.getTexturePaths("placeholder-asset-do-not-delete.png");
        Assert.assertNotNull(paths);
        Assert.assertEquals(1, paths.size());
        Assert.assertTrue(
                paths.get(0).endsWith("build/resources/main/placeholder-asset-do-not-delete.png"));
    }
}

package graphic.textures;

import com.badlogic.gdx.graphics.Texture;
import java.util.HashMap;
import java.util.Map;

public class TextureMap {
    private static final TextureMap INSTANCE = new TextureMap();
    private final Map<String, Texture> textureMap = new HashMap<>();

    private TextureMap() {}

    public static TextureMap getInstance() {
        return INSTANCE;
    }

    /**
     * Searches the HashMap for the matching texture and returns it. If the texture is not stored in
     * the HashMap, it is created and saved in.
     *
     * @param path to texture
     * @return the Texture
     */
    public Texture getTexture(String path) {
        if (!textureMap.containsKey(path)) {
            textureMap.put(path, new Texture(path));
        }

        return textureMap.get(path);
    }
}

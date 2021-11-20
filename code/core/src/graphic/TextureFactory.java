package graphic;

import com.badlogic.gdx.graphics.Texture;

/** Factory Pattern for Textures so we can mock it */
public class TextureFactory {
    /**
     * Loads the file and creates a texture.
     *
     * @param path to file
     * @return the texture
     */
    public Texture getTexture(String path) {
        return new Texture(path);
    }
}

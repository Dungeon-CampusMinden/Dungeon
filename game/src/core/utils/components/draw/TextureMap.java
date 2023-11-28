package core.utils.components.draw;

import com.badlogic.gdx.graphics.Texture;

import core.utils.components.path.IPath;

import java.util.HashMap;

/**
 * Maps Paths to libGDX {@link Texture}s, to reduce unnecessary loading of textures.
 *
 * <p>Use {@link #instance()} to get the only instance of the {@link TextureMap}, and use {@link
 * #textureAt(IPath)} to get the texture that is stored at the given path.
 *
 * @see Painter
 */
public final class TextureMap extends HashMap<IPath, Texture> {
    private static final TextureMap INSTANCE = new TextureMap();

    /**
     * Get the instance of the TextureMap.
     *
     * @return The only instance of the TextureMap.
     */
    public static TextureMap instance() {
        return INSTANCE;
    }

    /**
     * Searches the HashMap for the matching texture and returns it. If the texture is not stored in
     * the HashMap, it is created and saved.
     *
     * @param path Path to the texture.
     * @return The Texture at the given path.
     */
    public Texture textureAt(final IPath path) {
        if (!containsKey(path)) {
            put(path, new Texture(path.pathString()));
        }

        return get(path);
    }
}

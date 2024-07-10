package de.fwatermann.dungine.graphics.texture.atlas;

import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.utils.annotations.Null;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a texture atlas, a collection of textures packed into a single image. This class
 * allows for efficient texture management and rendering by minimizing the number of texture binds
 * required during drawing operations. It supports adding textures dynamically and organizes them
 * into one or more atlas textures based on size constraints.
 */
public class TextureAtlas {

  public static final int DEFAULT_ATLAS_SIZE = 1024;

  private final int width, height;

  protected final List<AtlasTexture> atlases = new ArrayList<>();
  protected final Map<Resource, AtlasEntry> entries = new HashMap<>();

  /**
   * Constructs a TextureAtlas with specified width, height, and resources. If resources are
   * provided, they are added to the atlas.
   *
   * @param width the width of the atlas
   * @param height the height of the atlas
   * @param resources a collection of resources to be added to the atlas; can be null
   */
  public TextureAtlas(int width, int height, @Null Collection<Resource> resources) {
    this.width = width;
    this.height = height;
    if (resources != null) resources.forEach(this::add);
  }

  /**
   * Constructs a TextureAtlas with specified width and height. No initial resources are added.
   *
   * @param width the width of the atlas
   * @param height the height of the atlas
   */
  public TextureAtlas(int width, int height) {
    this(width, height, null);
  }

  /**
   * Constructs a TextureAtlas with a default size, adding the provided resources.
   *
   * @param resources a list of resources to be added to the atlas
   */
  public TextureAtlas(List<Resource> resources) {
    this(DEFAULT_ATLAS_SIZE, DEFAULT_ATLAS_SIZE, resources);
  }

  /** Constructs a TextureAtlas with a default size and no initial resources. */
  public TextureAtlas() {
    this(DEFAULT_ATLAS_SIZE, DEFAULT_ATLAS_SIZE, null);
  }

  /**
   * Add a resource (must be a texture) to the TextureAtlas. The resource is added to the first
   * available AtlasTexture. If no AtlasTexture has enough space, a new one is created.
   *
   * @param resource the resource to add to the TextureAtlas
   * @return the AtlasEntry that represents the position of the resource in the TextureAtlas or null
   *     if the resource could not be added
   */
  public AtlasEntry add(Resource resource) {
    for (int i = 0; i < this.atlases.size(); i++) {
      AtlasNode node = this.atlases.get(i).add(resource, 1);
      if (node != null) {
        AtlasEntry ret = new AtlasEntry(this, i, node);
        this.entries.put(resource, ret);
        return ret;
      }
    }
    AtlasTexture newTexture = new AtlasTexture(this.width, this.height);
    this.atlases.add(newTexture);
    AtlasNode node = newTexture.add(resource, 1);
    if (node != null) {
      return new AtlasEntry(this, this.atlases.size() - 1, node);
    }
    return null;
  }

  /**
   * Add a collection of resources to the TextureAtlas.
   *
   * @param resources the resources to add to the TextureAtlas
   */
  public void addAll(Collection<Resource> resources) {
    resources.forEach(this::add);
  }

  /**
   * Get the AtlasEntry for a given resource. Returns null if the resource is not in the atlas.
   *
   * @param resource the resource to get the AtlasEntry for
   * @return the AtlasEntry for the resource or null if the resource is not in the atlas
   */
  public AtlasEntry get(Resource resource) {
    return this.entries.get(resource);
  }

  /**
   * Save the TextureAtlas to the filesystem.
   *
   * <p>The atlas is saved as a series of images, one for each AtlasTexture.
   *
   * @param path the path to save the atlas to
   */
  public void saveAtlas(Path path) {
    for (int i = 0; i < this.atlases.size(); i++) {
      this.atlases.get(i).save(path.resolve("atlas" + i + ".png"));
    }
  }
}

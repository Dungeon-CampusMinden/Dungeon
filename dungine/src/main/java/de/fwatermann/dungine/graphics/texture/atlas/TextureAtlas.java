package de.fwatermann.dungine.graphics.texture.atlas;

import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.utils.GLUtils;
import de.fwatermann.dungine.utils.annotations.Null;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

/**
 * Represents a texture atlas, a collection of textures packed into a single image. This class
 * allows for efficient texture management and rendering by minimizing the number of texture binds
 * required during drawing operations. It supports adding textures dynamically and organizes them
 * into one or more atlas textures based on size constraints.
 */
public class TextureAtlas {

  private static final Logger LOGGER = LogManager.getLogger(TextureAtlas.class);

  /**
   * The default size of the atlas
   *
   * <p>Set to 1024 as OpenGL 3.3 must support at least 1024x1024 textures.
   */
  public static final int DEFAULT_ATLAS_SIZE = 1024;

  /**
   * The maximum number of pages in the atlas. Each page is a separat texture.
   *
   * <p>Set to 10 as OpenGL 3.3 must support at least 16 active texture units, and we may need some for
   * other textures. The first 6 (GL_TEXTURE0 - GL_TEXTURE5) texture units are left free.
   */
  public static int MAX_PAGES = 10;

  /**
   * The maximum number of entries in the atlas
   *
   * <p>This limit is based on the memory layout of the UBO used to pass the atlas data to the
   * shader.
   */
  public static int MAX_ENTRIES = 65536;

  /** The uniform buffer binding point for the TextureAtlas UBO. */
  public static final int UNIFORM_BUFFER_BINDING = 35;

  private final int width, height;

  /** The pages of the atlas. */
  protected final List<AtlasPage> pages = new ArrayList<>();

  /** The entries in the atlas. */
  protected final Map<Integer, AtlasEntry> entries = new HashMap<>();

  /** The indices of the resources in the atlas. */
  protected final Map<Resource, Integer> indices = new HashMap<>();

  private int glTBO = -1;
  private int glBO = -1;
  private ByteBuffer uboData;
  private boolean uboDirty = false;

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
    if (this.entries.size() >= MAX_ENTRIES) {
      LOGGER.error("TextureAtlas is full, cannot add resource: {}", resource);
      throw new IllegalStateException(
          "TextureAtlas is full! The maximum number of entries is reached: " + MAX_ENTRIES);
    }
    for (int i = 0; i < this.pages.size(); i++) {
      AtlasNode node = this.pages.get(i).add(resource, 0);
      if (node != null) {
        return this.addEntry(resource, i, node);
      }
    }

    if (this.pages.size() >= MAX_PAGES) {
      LOGGER.error("Not enough space in TextureAtlas! Cannot add resource: {}", resource);
      throw new IllegalStateException(
          "This resource does not fit in the remaining space of the TextureAtlas!");
    }

    AtlasPage newTexture = this.addPage();
    AtlasNode node = newTexture.add(resource, 0);
    if (node != null) {
      return this.addEntry(resource, this.pages.size() - 1, node);
    }
    LOGGER.error(
        "Resource is to big for TextureAtlas. The maximum dimensions are {}x{}",
        this.width,
        this.height);
    throw new IllegalStateException("Resource is to big for TextureAtlas");
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
  public @Null AtlasEntry get(Resource resource) {
    int index = this.getIndex(resource);
    if (index == -1) return null;
    return this.entries.getOrDefault(index, null);
  }

  /**
   * Get the index of a resource in the TextureAtlas. Returns -1 if the resource is not in the
   * atlas.
   *
   * @param resource the resource to get the index of
   * @return the index of the resource in the TextureAtlas or -1 if the resource is not in the atlas
   */
  public int getIndex(Resource resource) {
    return this.indices.getOrDefault(resource, -1);
  }

  /**
   * Save the TextureAtlas to the filesystem.
   *
   * <p>The atlas is saved as a series of images, one for each AtlasTexture.
   *
   * <p>If the specified path does not exist, it is created.
   *
   * @param path the path to save the atlas to (must be a directory)
   * @throws IOException if an I/O error occurs
   */
  public void saveAtlas(Path path) throws IOException {
    if (Files.notExists(path)) {
      Files.createDirectories(path);
    }
    for (int i = 0; i < this.pages.size(); i++) {
      this.pages.get(i).save(path.resolve("atlas" + i + ".png"));
    }
  }

  private void initUBOData() {
    int capacity = 12 * this.entries.size();
    if (this.uboData == null) {
      this.uboData = BufferUtils.createByteBuffer(capacity);
    }
  }

  private AtlasPage addPage() {
    AtlasPage newTexture = new AtlasPage(this.width, this.height);
    this.pages.add(newTexture);
    return newTexture;
  }

  private AtlasEntry addEntry(Resource resource, int atlasPage, AtlasNode node) {
    if (this.uboData == null) this.initUBOData();
    AtlasEntry entry = new AtlasEntry(this, atlasPage, node);
    int index = this.entries.size();
    this.entries.put(index, entry);
    this.indices.put(resource, index);

    ByteBuffer old = this.uboData;
    ByteBuffer neu = BufferUtils.createByteBuffer(12 * this.entries.size());
    old.position(0);
    neu.position(0);
    neu.put(old);

    // x
    neu.putInt(
        ((entry.atlasNode.position.x & 0xFFFF) << 16) | (entry.atlasNode.position.y & 0xFFFF));
    // y
    neu.putInt(((entry.atlasNode.size.x & 0xFFFF) << 16) | (entry.atlasNode.size.y & 0xFFFF));
    // z
    neu.putInt((entry.atlasPage & 0xFFFF) << 16);
    neu.flip();

    this.uboData = neu;
    this.uboDirty = true;
    return entry;
  }

  /**
   * Use the TextureAtlas in a shader program.
   *
   * <p>This method binds the UBO to the specified shader programs uniform block.
   *
   * <p>The uniform block should be defined in the shader program as follows: <br>
   * <code>
   *   layout(std140) uniform TextureAtlas {<br>
   *    ivec2 size; // The size of the atlasPages in texels<br>
   *    sampler2D atlasPage[48]; // The textures of the atlasPages<br>
   *    ivec3 entries[65536]; // The entries in the atlas<br>
   *   }<br>
   * </code>
   *
   * <p>The x coordinate is stored in the lower 16 bits of the first int, the y coordinate in the
   * upper 16 bits. The width is stored in the lower 16 bits of the second int, the height in the
   * upper 16 bits. The atlas page index is stored in the lower 16 bits of the third int.
   *
   * @param program the shader program to use the TextureAtlas in
   * @param uniformAtlasPageSize the name of the uniform for the size of the atlas pages
   * @param uniformAtlasEntrySampler the name of the uniform for the sampler of the atlas entries
   * @param uniformPagesSamplerName the name of the uniform for the sampler of the atlas pages
   *     textures
   */
  public void use(
      ShaderProgram program,
      String uniformAtlasPageSize,
      String uniformAtlasEntrySampler,
      String uniformPagesSamplerName) {
    if (this.glBO == -1) {
      this.glBO = GL33.glGenBuffers();
      GLUtils.checkError();
    }
    if (this.uboDirty) {
      GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, this.glBO);
      GL33.glBufferData(GL33.GL_ARRAY_BUFFER, this.uboData, GL33.GL_STATIC_DRAW);
      GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
      this.uboDirty = false;
      GLUtils.checkError();
    }
    if (this.glTBO == -1) {
      this.glTBO = GL33.glGenTextures();
      GL33.glBindTexture(GL33.GL_TEXTURE_BUFFER, this.glTBO);
      GL33.glTexBuffer(GL33.GL_TEXTURE_BUFFER, GL33.GL_R32I, this.glBO);
      GL33.glBindTexture(GL33.GL_TEXTURE_BUFFER, 0);
      GL33.glBindBuffer(GL33.GL_TEXTURE_BUFFER, 0);
      GLUtils.checkError();
    }

    int firstUnit = 6;
    for (int i = 0; i < this.pages.size(); i++) {
      this.pages.get(i).texture.bind(GL33.GL_TEXTURE0 + firstUnit + i);
      program.setUniform1i(uniformPagesSamplerName + "[" + i + "]", firstUnit + i);
    }

    GL33.glActiveTexture(GL33.GL_TEXTURE0 + firstUnit - 1);
    GL33.glBindTexture(GL33.GL_TEXTURE_BUFFER, this.glTBO);
    GL33.glTexBuffer(GL33.GL_TEXTURE_BUFFER, GL33.GL_R32I, this.glBO);

    program.setUniform2iv(uniformAtlasPageSize, this.width, this.height);
    program.setUniform1i(uniformAtlasEntrySampler, firstUnit - 1);
    GL33.glActiveTexture(GL33.GL_TEXTURE0);
  }

  /**
   * Use the TextureAtlas in a shader program.
   *
   * <p>This method binds the UBO to the specified shader programs uniform block. It uses the
   * preconfigured uniform block name from the shader program configuration.
   *
   * @param program the shader program to use the TextureAtlas in
   * @param suffix the suffix to append to the uniform block names
   */
  public void use(ShaderProgram program, String suffix) {
    if (suffix == null) suffix = "";
    this.use(
        program,
        program.configuration().uniformTextureAtlasSize + suffix,
        program.configuration().uniformTextureAtlasEntrySampler + suffix,
        program.configuration().uniformTextureAtlasPagesSamplerArray + suffix);
  }

  /**
   * Use the TextureAtlas in a shader program.
   *
   * @param program the shader program to use the TextureAtlas in
   */
  public void use(ShaderProgram program) {
    this.use(program, null);
  }
}

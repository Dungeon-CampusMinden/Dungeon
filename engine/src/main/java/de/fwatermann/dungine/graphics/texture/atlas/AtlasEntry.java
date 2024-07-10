package de.fwatermann.dungine.graphics.texture.atlas;

import de.fwatermann.dungine.graphics.texture.Texture;

/**
 * Represents an entry in a texture atlas.
 * An atlas entry is a reference to a specific texture within a larger atlas,
 * identified by its index and position within the atlas.
 */
public class AtlasEntry {

  protected final int index;
  protected final AtlasNode atlasNode;
  protected final TextureAtlas atlas;

  /**
   * Constructs a new AtlasEntry.
   *
   * @param atlas The texture atlas this entry is part of.
   * @param index The index of the texture within the atlas.
   * @param atlasNode The node within the atlas where this texture is located.
   */
  protected AtlasEntry(TextureAtlas atlas, int index, AtlasNode atlasNode) {
    this.index = index;
    this.atlasNode = atlasNode;
    this.atlas = atlas;
  }

  /**
   * Returns the texture associated with this atlas entry.
   *
   * @return The texture.
   */
  public Texture texture() {
    return this.atlas.atlases.get(this.index).texture;
  }

  /**
   * Returns the x-coordinate of the texture within the atlas.
   *
   * @return The x-coordinate.
   */
  public int x() {
    return this.atlasNode.position.x;
  }

  /**
   * Returns the y-coordinate of the texture within the atlas.
   *
   * @return The y-coordinate.
   */
  public int y() {
    return this.atlasNode.position.y;
  }

  /**
   * Returns the width of the texture within the atlas.
   *
   * @return The width.
   */
  public int width() {
    return this.atlasNode.size.x;
  }

  /**
   * Returns the height of the texture within the atlas.
   *
   * @return The height.
   */
  public int height() {
    return this.atlasNode.size.y;
  }
}

package de.fwatermann.dungine.graphics.texture.atlas;

import de.fwatermann.dungine.graphics.texture.Texture;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.utils.GLUtils;
import de.fwatermann.dungine.utils.ThreadUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageWrite;

/**
 * Represents a texture atlas, a large image containing a collection of smaller images. This class
 * provides functionality to add textures to the atlas and save the atlas to a file.
 */
public class AtlasPage {

  protected Texture texture;
  protected final AtlasNode rootNode;
  protected final int width, height;

  /**
   * Constructs a new AtlasTexture with specified dimensions.
   *
   * @param width The width of the atlas in pixels.
   * @param height The height of the atlas in pixels.
   */
  protected AtlasPage(int width, int height) {
    this.width = width;
    this.height = height;
    this.rootNode = new AtlasNode(0, 0, width, height);
  }

  /**
   * Attempts to add a resource to the atlas with specified padding.
   *
   * @param resource The resource to add to the atlas.
   * @param padding The padding around the resource in pixels.
   * @return The node representing the location of the resource in the atlas, or null if it could
   *     not be added.
   */
  protected AtlasNode add(Resource resource, int padding) {
    AtlasNode node = this.rootNode.insert(resource, padding);
    if (node != null) {
      this.draw(node);
    }
    return node;
  }

  /**
   * Draws the resource represented by the given node onto the atlas.
   *
   * @param node The node representing the resource to draw.
   */
  protected void draw(AtlasNode node) {
    ThreadUtils.checkMainThread();
    if (this.texture == null) {
      this.texture = new Texture(this.width, this.height);
    }
    try {
      ByteBuffer buffer =
          STBImage.stbi_load_from_memory(
              node.resource.readBytes(), new int[1], new int[1], new int[1], 4);
      node.resource.deallocate();
      if (buffer == null) {
        throw new IOException("Failed to load image");
      }
      GLUtils.checkBuffer(buffer);
      int currentTexture = GL33.glGetInteger(GL33.GL_TEXTURE_BINDING_2D);
      GL33.glBindTexture(GL33.GL_TEXTURE_2D, this.texture.glHandle());
      GL33.glTexSubImage2D(
          GL33.GL_TEXTURE_2D,
          0,
          node.position.x,
          node.position.y,
          node.size.x,
          node.size.y,
          GL33.GL_RGBA,
          GL33.GL_UNSIGNED_BYTE,
          buffer);
      GL33.glBindTexture(GL33.GL_TEXTURE_2D, currentTexture);

      STBImage.stbi_image_free(buffer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Saves the atlas to a file in PNG format.
   *
   * @param path The file path to save the atlas to.
   */
  public void save(Path path) {
    ByteBuffer buffer = BufferUtils.createByteBuffer(this.width * this.height * 4);
    int currentTexture = GL33.glGetInteger(GL33.GL_TEXTURE_BINDING_2D);
    GL33.glBindTexture(GL33.GL_TEXTURE_2D, this.texture.glHandle());
    GL33.glGetTexImage(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, buffer);
    GL33.glBindTexture(GL33.GL_TEXTURE_2D, currentTexture);
    STBImageWrite.stbi_write_png(
        path.toString(), this.width, this.height, 4, buffer, this.width * 4);
  }
}

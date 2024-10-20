package de.fwatermann.dungine.graphics.texture.atlas;

import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.utils.ImageUtils;
import de.fwatermann.dungine.utils.annotations.Nullable;
import org.joml.Vector2i;

/**
 * Represents a node within a texture atlas. This class is used to manage the positioning and
 * allocation of textures within the atlas. Each node can either be a leaf node, representing an
 * allocated space for a texture, or an internal node, which divides the space into smaller regions.
 */
public class AtlasNode {

  /**
   * The position of the texture within the page texture in pixels.
   */
  protected final Vector2i position;

  /**
   * The size of the texture in pixels.
   */
  protected final Vector2i size;

  /**
   * The resource associated with this node. If the node is a leaf node, this is the texture otherwise
   * it is null.
   */
  @Nullable
  protected Resource resource;

  private final AtlasNode[] children;

  /**
   * Constructs a new AtlasNode with specified position and size.
   *
   * @param x The x-coordinate of the node's position within the atlas.
   * @param y The y-coordinate of the node's position within the atlas.
   * @param width The width of the node.
   * @param height The height of the node.
   */
  protected AtlasNode(int x, int y, int width, int height) {
    this.position = new Vector2i(x, y);
    this.size = new Vector2i(width, height);
    this.children = new AtlasNode[2];
  }

  /**
   * Checks if the node is a leaf node. A node is considered a leaf if it has no children.
   *
   * @return true if the node is a leaf, false otherwise.
   */
  protected boolean isLeaf() {
    return this.children[0] == null && this.children[1] == null;
  }

  /**
   * Attempts to insert a resource into the atlas, starting at this node. If the node is an internal
   * node, the method recursively tries to insert the resource into its children. If the node is a
   * leaf and is unoccupied, it checks if the resource fits within the node's dimensions.
   *
   * @param resource The resource to insert into the atlas.
   * @param padding The padding to apply around the resource. This affects the subdivision of nodes.
   * @return The node where the resource was inserted, or null if the insertion was unsuccessful.
   */
  protected AtlasNode insert(Resource resource, int padding) {
    ImageUtils.ImageInfo imageInfo = ImageUtils.getImageSize(resource);
    if (!this.isLeaf()) {
      AtlasNode newNode = this.children[0].insert(resource, padding);
      if (newNode != null) {
        return newNode;
      } else {
        return this.children[1].insert(resource, padding);
      }
    } else {
      if (this.resource != null) { // Node occupied
        return null;
      }
      if (imageInfo.width() > this.size.x || imageInfo.height() > this.size.y) {
        return null;
      }
      if (imageInfo.width() == this.size.x && imageInfo.height() == this.size.y) { // Perfect fit
        this.resource = resource;
        return this;
      }

      int dw = this.size.x - imageInfo.width();
      int dh = this.size.y - imageInfo.height();

      if (dw > dh) {
        this.children[0] =
            new AtlasNode(this.position.x, this.position.y, imageInfo.width(), this.size.y);
        this.children[1] =
            new AtlasNode(
                this.position.x + imageInfo.width() + padding,
                this.position.y,
                this.size.x - imageInfo.width() - padding,
                this.size.y);
      } else {
        this.children[0] =
            new AtlasNode(this.position.x, this.position.y, this.size.x, imageInfo.height());
        this.children[1] =
            new AtlasNode(
                this.position.x,
                this.position.y + imageInfo.height() + padding,
                this.size.x,
                this.size.y - imageInfo.height() - padding);
      }
      return this.children[0].insert(resource, padding);
    }
  }
}

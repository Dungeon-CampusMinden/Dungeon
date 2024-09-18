package de.fwatermann.dungine.graphics.simple;

import de.fwatermann.dungine.graphics.scene.model.Material;
import de.fwatermann.dungine.graphics.texture.animation.Animation;

public class CuboidTextured extends Cuboid {

  private final Animation[] animations = new Animation[6];

  /**
   * Constructs a new textured cuboid with an animation for all faces.
   * @param animation the animation to use for all faces
   */
  public CuboidTextured(Animation animation) {
    for(int i = 0; i < 6; i++) {
      this.animations[i] = animation;
    }
  }

  public CuboidTextured(Animation vertical, Animation horizontal) {
    this.animations[0] = horizontal;
    this.animations[1] = horizontal;
    this.animations[2] = horizontal;
    this.animations[3] = horizontal;
    this.animations[4] = vertical;
    this.animations[5] = vertical;
  }

  public CuboidTextured(Animation front, Animation back, Animation left, Animation right, Animation top, Animation bottom) {
    this.animations[0] = front;
    this.animations[1] = back;
    this.animations[2] = left;
    this.animations[3] = right;
    this.animations[4] = top;
    this.animations[5] = bottom;
  }

  @Override
  protected void initMaterials() {
    for(int i = 0; i < 6; i ++) {
      Material material = new Material();
      material.diffuseTexture = this.animations[i];
      material.meshes.add(new Material.MeshEntry(MESH, i * 6 * 2, 6));
      material.flags = Material.MATERIAL_FLAG_HAS_DIFFUSE_TEXTURE;
      this.materials.add(material);
    }
  }
}

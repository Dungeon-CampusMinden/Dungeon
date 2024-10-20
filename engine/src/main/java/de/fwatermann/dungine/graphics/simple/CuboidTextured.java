package de.fwatermann.dungine.graphics.simple;

import de.fwatermann.dungine.graphics.scene.model.Material;
import de.fwatermann.dungine.graphics.texture.animation.Animation;

/**
 * The `CuboidTextured` class represents a textured cuboid model that can be rendered in a graphics scene.
 * It extends the `Cuboid` class and provides constructors to initialize the cuboid with different animations
 * for its faces. It also overrides the `initMaterials` method to set up the materials for rendering.
 */
public class CuboidTextured extends Cuboid {

  private final Animation[] animations = new Animation[6];

  /**
   * Constructs a new textured cuboid with an animation for all faces.
   *
   * @param animation the animation to use for all faces
   */
  public CuboidTextured(Animation animation) {
    for(int i = 0; i < 6; i++) {
      this.animations[i] = animation;
    }
  }

  /**
   * Constructs a new textured cuboid with separate animations for vertical and horizontal faces.
   *
   * @param vertical the animation to use for the vertical faces (top and bottom)
   * @param horizontal the animation to use for the horizontal faces (front, back, left, and right)
   */
  public CuboidTextured(Animation vertical, Animation horizontal) {
    this.animations[0] = horizontal;
    this.animations[1] = horizontal;
    this.animations[2] = horizontal;
    this.animations[3] = horizontal;
    this.animations[4] = vertical;
    this.animations[5] = vertical;
  }

  /**
   * Constructs a new textured cuboid with separate animations for each face.
   *
   * @param front the animation to use for the front face
   * @param back the animation to use for the back face
   * @param left the animation to use for the left face
   * @param right the animation to use for the right face
   * @param top the animation to use for the top face
   * @param bottom the animation to use for the bottom face
   */
  public CuboidTextured(Animation front, Animation back, Animation left, Animation right, Animation top, Animation bottom) {
    this.animations[0] = front;
    this.animations[1] = back;
    this.animations[2] = left;
    this.animations[3] = right;
    this.animations[4] = top;
    this.animations[5] = bottom;
  }

  /**
   * Initializes the materials for the cuboid, setting up the diffuse texture and mesh entries for each face.
   */
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

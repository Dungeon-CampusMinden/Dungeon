package de.fwatermann.dungine.graphics.simple;

import de.fwatermann.dungine.graphics.scene.model.Material;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.graphics.texture.Texture;
import de.fwatermann.dungine.graphics.texture.animation.Animation;
import de.fwatermann.dungine.graphics.texture.animation.ArrayAnimation;
import de.fwatermann.dungine.resource.Resource;

/**
 * The `PaneTextured` class represents a textured pane that can be rendered in a scene. It extends
 * the `Pane` class and provides methods to set and get the animation of the pane.
 */
public class PaneTextured extends Pane {

  /** The shader program used for rendering the pane. */
  private static ShaderProgram SHADER;

  /** The animation used for the pane's texture. */
  private Animation animation;

  /** The material used for the pane. */
  private Material material;

  /**
   * Constructs a `PaneTextured` instance with the specified animation.
   *
   * @param animation the animation to use for the pane's texture
   */
  public PaneTextured(Animation animation) {
    this.animation = animation;
  }

  /**
   * Constructs a `PaneTextured` instance with the specified resource.
   *
   * @param resource the resource to use for the pane's texture
   */
  public PaneTextured(Resource resource) {
    this.animation = ArrayAnimation.of(resource);
  }

  /**
   * Constructs a `PaneTextured` instance with the specified texture.
   *
   * @param texture the texture to use for the pane's texture
   */
  public PaneTextured(Texture texture) {
    this.animation = ArrayAnimation.of(texture);
  }

  /** Initializes the materials for the pane. */
  @Override
  protected void initMaterials() {
    this.material = new Material();
    this.material.diffuseColor.set(1.0f);
    this.material.diffuseTexture = this.animation;
    this.material.flags = Material.MATERIAL_FLAG_HAS_DIFFUSE_TEXTURE;
    this.material.meshes.add(new Material.MeshEntry(MESH, 0, 0));
    this.materials.add(this.material);
  }

  /**
   * Gets the animation of the pane.
   *
   * @return the animation of the pane
   */
  public Animation animation() {
    return this.animation;
  }

  /**
   * Sets the animation of the pane.
   *
   * @param animation the new animation to use for the pane's texture
   * @return this `PaneTextured` instance for method chaining
   */
  public PaneTextured animation(Animation animation) {
    this.animation = animation;
    if (this.material != null) {
      this.material.diffuseTexture = this.animation;
    }
    return this;
  }
}

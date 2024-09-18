package de.fwatermann.dungine.graphics.simple;

import de.fwatermann.dungine.graphics.scene.model.Material;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.graphics.texture.Texture;
import de.fwatermann.dungine.graphics.texture.animation.Animation;
import de.fwatermann.dungine.graphics.texture.animation.ArrayAnimation;
import de.fwatermann.dungine.resource.Resource;

public class PaneTextured extends Pane {

  private static ShaderProgram SHADER;

  private Animation animation;
  private Material material;

  public PaneTextured(Animation animation) {
    this.animation = animation;
  }

  public PaneTextured(Resource resource) {
    this.animation = ArrayAnimation.of(resource);
  }

  public PaneTextured(Texture texture) {
    this.animation = ArrayAnimation.of(texture);
  }

  @Override
  protected void initMaterials() {
    this.material = new Material();
    this.material.diffuseColor.set(1.0f);
    this.material.diffuseTexture = this.animation;
    this.material.flags = Material.MATERIAL_FLAG_HAS_DIFFUSE_TEXTURE;
    this.material.meshes.add(new Material.MeshEntry(MESH, 0, 0));
    this.materials.add(this.material);
  }

  public Animation animation() {
    return this.animation;
  }

  public PaneTextured animation(Animation animation) {
    this.animation = animation;
    if(this.material != null) {
      this.material.diffuseTexture = this.animation;
    }
    return this;
  }
}

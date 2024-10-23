package de.fwatermann.dungine.graphics.scene.model;

import de.fwatermann.dungine.graphics.Renderable;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraFrustum;
import de.fwatermann.dungine.graphics.scene.SceneRenderer;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.graphics.texture.animation.Animation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.lwjgl.opengl.GL33;

/**
 * The `Model` class represents a 3D model in the game engine. It contains materials and provides
 * methods for rendering the model.
 */
public class Model extends Renderable<Model> {

  protected boolean forceIlluminate = false;

  /** The list of materials associated with this model. */
  protected final List<Material> materials = new ArrayList<>();

  /** The shader program used for rendering the model. */
  protected ShaderProgram shader;

  /** Default constructor for the `Model` class. */
  protected Model() {}

  /**
   * Constructs a new `Model` with the specified materials.
   *
   * @param materials the list of materials to associate with this model
   */
  protected Model(List<Material> materials) {
    this.materials.addAll(materials);
  }

  /**
   * Renders the model using the specified camera.
   *
   * @param camera the camera to use for rendering
   */
  @Override
  public void render(Camera<?> camera) {
    if (this.shader == null) {
      this.render(camera, SceneRenderer.defaultShader());
    } else {
      this.render(camera, this.shader);
    }
  }

  /**
   * Renders the model using the specified camera and shader program.
   *
   * @param camera the camera to use for rendering
   * @param shader the shader program to use for rendering
   */
  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    shader.bind();
    shader.useCamera(camera);

    for (Material material : this.materials) {
      shader.useMaterial(material);

      GL33.glBlendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA);

      if (material.diffuseTexture != null) {
        material.diffuseTexture.bind(shader, Animation.AnimationSlot.ANIMATION_0, GL33.GL_TEXTURE0);
      }
      if (material.ambientTexture != null) {
        material.ambientTexture.bind(shader, Animation.AnimationSlot.ANIMATION_1, GL33.GL_TEXTURE2);
      }
      if (material.specularTexture != null) {
        material.specularTexture.bind(
            shader, Animation.AnimationSlot.ANIMATION_2, GL33.GL_TEXTURE4);
      }
      if (material.normalTexture != null) {
        material.normalTexture.bind(shader, Animation.AnimationSlot.ANIMATION_3, GL33.GL_TEXTURE6);
      }

      shader.setUniform1i(
          shader.configuration().uniformForceIlluminate, this.forceIlluminate ? 1 : 0);

      material.meshes.forEach(
          meshEntry -> {
            meshEntry.mesh().transformation(this.position(), this.rotation(), this.scaling());
            if (meshEntry.offset() == 0 && meshEntry.count() <= 0) {
              meshEntry.mesh().render(camera, shader);
            } else {
              meshEntry.mesh().render(camera, shader, meshEntry.offset(), meshEntry.count());
            }
          });
    }

    shader.unbind();
  }

  /**
   * Determines whether the model should be rendered based on the camera frustum.
   *
   * @param frustum the camera frustum to check against
   * @return true if the model should be rendered, false otherwise
   */
  @Override
  public boolean shouldRender(CameraFrustum frustum) {
    return true;
    // TODO: Implement frustum culling for models.
  }

  /**
   * Returns a stream of materials associated with this model.
   *
   * @return a stream of materials
   */
  public Stream<Material> materials() {
    return this.materials.stream();
  }

  /**
   * Returns the shader program used for rendering the model.
   *
   * @return the shader program
   */
  public ShaderProgram shader() {
    return this.shader;
  }

  /**
   * Sets the shader program used for rendering the model.
   *
   * @param shader the shader program to set
   * @return this model for method chaining
   */
  public Model shader(ShaderProgram shader) {
    this.shader = shader;
    return this;
  }

  /**
   * Determines whether the model should be force-illuminated.
   *
   * @return true if the model should be force-illuminated, false otherwise
   */
  public boolean forceIlluminate() {
    return this.forceIlluminate;
  }

  /**
   * Sets whether the model should be force illuminated.
   *
   * @param forceIlluminate the forceIlluminate to set
   * @return this model for method chaining
   */
  public Model forceIlluminate(boolean forceIlluminate) {
    this.forceIlluminate = forceIlluminate;
    return this;
  }
}

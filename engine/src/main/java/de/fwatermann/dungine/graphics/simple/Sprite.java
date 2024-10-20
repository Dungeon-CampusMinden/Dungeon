package de.fwatermann.dungine.graphics.simple;

import de.fwatermann.dungine.graphics.BillboardMode;
import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.Renderable;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraFrustum;
import de.fwatermann.dungine.graphics.mesh.ArrayMesh;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.graphics.texture.animation.Animation;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.utils.ThreadUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;

/**
 * Represents a sprite that can be rendered. Implements the IRenderable interface to provide
 * rendering functionality.
 */
public class Sprite extends Renderable<Sprite> {

  private static ShaderProgram SHADER;
  private static ArrayMesh MESH;

  private BillboardMode billboardMode;
  private float width, height;
  private Animation animation;

  /**
   * Constructs a new sprite with the specified animation, width, height, and billboard mode.
   *
   * @param animation The animation to be used for this sprite.
   * @param width The width of the sprite.
   * @param height The height of the sprite.
   * @param billboardMode The billboard mode of the sprite.
   */
  public Sprite(Animation animation, float width, float height, BillboardMode billboardMode) {
    this.animation = animation;
    this.width = width;
    this.height = height;
    this.billboardMode = billboardMode;
  }

  /**
   * Renders the sprite using the specified camera. Initializes the shader if it is not already
   * initialized.
   *
   * @param camera The camera to be used for rendering.
   */
  @Override
  public void render(Camera<?> camera) {
    if (SHADER == null) {
      try {
        Shader vertexShader =
            Shader.loadShader(
                Resource.load("/shaders/3d/Sprite.vsh"), Shader.ShaderType.VERTEX_SHADER);
        Shader fragmentShader =
            Shader.loadShader(
                Resource.load("/shaders/3d/Sprite.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
        SHADER = new ShaderProgram(vertexShader, fragmentShader);
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }
    this.render(camera, SHADER);
  }

  /**
   * Renders the sprite using the specified camera and shader program. Initializes the mesh if it is
   * not already initialized.
   *
   * @param camera The camera to be used for rendering.
   * @param shader The shader program to be used for rendering.
   */
  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    if (MESH == null) {
      initMesh();
    }
    shader.bind();
    shader.useAnimation(this.animation);
    shader.setUniform1i("uBillboardMode", this.billboardMode.value);
    MESH.transformation(this.position(), this.rotation(), this.scaling());
    MESH.render(camera, shader);
    shader.unbind();
  }

  /**
   * Sets the animation to be used on this sprite.
   *
   * @param animation the animation to be used from now on.
   */
  public void animation(Animation animation) {
    this.animation = animation;
  }

  /**
   * Returns the width of the sprite.
   *
   * @return The width of the sprite.
   */
  public float width() {
    return this.width;
  }

  /**
   * Sets the width of the sprite.
   *
   * @param width The new width of the sprite.
   * @return The current instance of the sprite.
   */
  public Sprite width(float width) {
    this.width = width;
    return this;
  }

  /**
   * Returns the height of the sprite.
   *
   * @return The height of the sprite.
   */
  public float height() {
    return this.height;
  }

  /**
   * Sets the height of the sprite.
   *
   * @param height The new height of the sprite.
   * @return The current instance of the sprite.
   */
  public Sprite height(float height) {
    this.height = height;
    return this;
  }

  /**
   * Returns the billboard mode of the sprite.
   *
   * @return The billboard mode of the sprite.
   */
  public BillboardMode billboardMode() {
    return this.billboardMode;
  }

  /**
   * Sets the billboard mode of the sprite.
   *
   * @param billboardMode The new billboard mode of the sprite.
   * @return The current instance of the sprite.
   */
  public Sprite billboardMode(BillboardMode billboardMode) {
    this.billboardMode = billboardMode;
    return this;
  }

  private static void initMesh() {
    ThreadUtils.checkMainThread();
    ByteBuffer vertices = BufferUtils.createByteBuffer(5 * 4 * 4);
    vertices
        .asFloatBuffer()
        .position(0)
        .put(
            new float[] {
              -0.5f, -0.5f, 0.0f, 0.0f, 1.0f,
              +0.5f, -0.5f, 0.0f, 1.0f, 1.0f,
              -0.5f, +0.5f, 0.0f, 0.0f, 0.0f,
              +0.5f, +0.5f, 0.0f, 1.0f, 0.0f
            });
    MESH =
        new ArrayMesh(
            vertices,
            PrimitiveType.TRIANGLE_STRIP,
            GLUsageHint.DRAW_STATIC,
            new VertexAttribute(3, DataType.FLOAT, "aPosition"),
            new VertexAttribute(2, DataType.FLOAT, "aTexCoord"));
  }

  @Override
  public boolean shouldRender(CameraFrustum frustum) {
    return true;
    // TODO: Implement frustum culling for Sprites.
  }
}

package de.fwatermann.dungine.graphics.simple;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraFrustum;
import de.fwatermann.dungine.graphics.mesh.ArrayMesh;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.scene.model.Material;
import de.fwatermann.dungine.graphics.scene.model.Model;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.utils.BoundingBox;
import java.nio.ByteBuffer;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

/**
 * The `Pane` class represents a simple graphical pane that can be rendered in a scene.
 * It extends the `Model` class and provides methods to set and get the color of the pane,
 * initialize materials, and handle transformations.
 */
public class Pane extends Model {

  /** The color of the pane in RGBA format. */
  private int color;

  /** The mesh used for rendering the pane. */
  protected static ArrayMesh MESH;

  /** The bounding box of the pane. */
  protected BoundingBox boundingBox;

  /** Constructs a new `Pane` instance and initializes its mesh. */
  public Pane() {
    initMesh();
  }

  /** Initializes the mesh for the pane. */
  private static void initMesh() {
    ByteBuffer vertices = BufferUtils.createByteBuffer(4 * 4 * 8);
    vertices
        .asFloatBuffer()
        .position(0)
        .put(
            new float[] {
              -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f,
                  1.0f, 0.0f,
              -0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 1.0f,
                  1.0f, 1.0f
            });
    MESH =
        new ArrayMesh(
            vertices,
            PrimitiveType.TRIANGLE_STRIP,
            GLUsageHint.DRAW_STATIC,
            new VertexAttribute(3, DataType.FLOAT, "aPosition"),
            new VertexAttribute(3, DataType.FLOAT, "aNormal"),
            new VertexAttribute(2, DataType.FLOAT, "aTexCoord"));
  }

  /** Initializes the materials for the pane. */
  protected void initMaterials() {
    Material material = new Material();
    material.diffuseColor.set(
        ((this.color >> 24) & 0xFF) / 255.0f,
        ((this.color >> 16) & 0xFF) / 255.0f,
        ((this.color >> 8) & 0xFF) / 255.0f,
        (this.color & 0xFF) / 255.0f);
    material.meshes.add(new Material.MeshEntry(MESH, 0, 4));
    this.materials.add(material);
  }

  /**
   * Renders the pane using the specified camera and shader program.
   *
   * @param camera the camera to use for rendering
   * @param shader the shader program to use for rendering
   */
  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    if (this.materials.isEmpty()) {
      this.initMaterials();
    }
    super.render(camera, shader);
  }

  /**
   * Gets the color of the pane.
   *
   * @return the color of the pane
   */
  public int color() {
    return this.color;
  }

  /**
   * Sets the color of the pane.
   *
   * @param rgba the new color of the pane in RGBA format
   */
  public void color(int rgba) {
    this.color = rgba;
  }

  /**
   * Sets the color of the pane.
   *
   * @param r the red component of the color
   * @param g the green component of the color
   * @param b the blue component of the color
   * @param a the alpha component of the color
   */
  public void color(int r, int g, int b, int a) {
    this.color = (a << 24) | (r << 16) | (g << 8) | b;
  }

  /**
   * Sets the color of the pane.
   *
   * @param r the red component of the color
   * @param g the green component of the color
   * @param b the blue component of the color
   * @param a the alpha component of the color
   */
  public void color(float r, float g, float b, float a) {
    this.color((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
  }

  /** Updates the bounding box when the transformation changes. */
  @Override
  protected void transformationChanged() {
    super.transformationChanged();
    Vector4f min = this.transformationMatrix().transform(new Vector4f(-0.5f, -0.5f, 0.0f, 1.0f));
    Vector4f max = this.transformationMatrix().transform(new Vector4f(0.5f, 0.5f, 0.0f, 1.0f));
    this.boundingBox = new BoundingBox(min.x, min.y, min.z, max.x, max.y, max.z);
  }

  /**
   * Determines whether the pane should be rendered based on the camera frustum.
   *
   * @param frustum the camera frustum to check against
   * @return true if the pane should be rendered, false otherwise
   */
  @Override
  public boolean shouldRender(CameraFrustum frustum) {
    int frustumResult = frustum.intersectAab(this.boundingBox.getMin(), this.boundingBox.getMax());
    return frustumResult == CameraFrustum.INTERSECT || frustumResult == CameraFrustum.INSIDE;
  }
}

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

public class Pane extends Model {

  private int color;

  protected static ArrayMesh MESH;
  protected BoundingBox boundingBox;

  public Pane() {
    initMesh();
  }

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

  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    if (this.materials.isEmpty()) {
      this.initMaterials();
    }
    super.render(camera, shader);
  }

  /**
   * Get the color of the plane.
   *
   * @return the color of the plane
   */
  public int color() {
    return this.color;
  }

  /**
   * Set the color of the plane.
   *
   * @param rgba the new color of the plane in RGBA format.
   */
  public void color(int rgba) {
    this.color = rgba;
  }

  /**
   * Set the color of the plane.
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
   * Set the color of the plane.
   *
   * @param r the red component of the color
   * @param g the green component of the color
   * @param b the blue component of the color
   * @param a the alpha component of the color
   */
  public void color(float r, float g, float b, float a) {
    this.color((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
  }

  @Override
  protected void transformationChanged() {
    super.transformationChanged();
    Vector4f min = this.transformationMatrix().transform(new Vector4f(-0.5f, -0.5f, 0.0f, 1.0f));
    Vector4f max = this.transformationMatrix().transform(new Vector4f(0.5f, 0.5f, 0.0f, 1.0f));
    this.boundingBox = new BoundingBox(min.x, min.y, min.z, max.x, max.y, max.z);
  }

  @Override
  public boolean shouldRender(CameraFrustum frustum) {
    int frustumResult = frustum.intersectAab(this.boundingBox.getMin(), this.boundingBox.getMax());
    return frustumResult == CameraFrustum.INTERSECT || frustumResult == CameraFrustum.INSIDE;
  }
}

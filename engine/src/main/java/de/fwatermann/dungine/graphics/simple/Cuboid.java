package de.fwatermann.dungine.graphics.simple;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.camera.CameraFrustum;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.IndexDataType;
import de.fwatermann.dungine.graphics.mesh.IndexedMesh;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.scene.model.Material;
import de.fwatermann.dungine.graphics.scene.model.Model;
import de.fwatermann.dungine.utils.BoundingBox;
import java.nio.ByteBuffer;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

public class Cuboid extends Model {

  protected static IndexedMesh MESH;
  private BoundingBox boundingBox = new BoundingBox(0, 0, 0, 0, 0, 0);

  private int color = 0xDDDDDDFF;

  /**
   * Constructs a new Cuboid with the specified position and color.
   * @param rgb the color of the cuboid in RGBA format
   */
  public Cuboid(int rgb) {
    initMesh();
    this.color = rgb;
    Material material = new Material();
    material.diffuseColor.set(0.8f);
    material.meshes.add(MESH);
    this.materials.add(material);
  }

  public Cuboid() {
    this(0xDDDDDDFF);
  }

  private static void initMesh() {
    if(MESH != null) return;
    ByteBuffer vertices = BufferUtils.createByteBuffer(4 * 6 * 6 * 4);
    vertices
        .asFloatBuffer()
        .position(0)
        .put(
            new float[] {
              //Format: x, y, z, nx, ny, nz, tx, ty, tz, bix, biy, biz

              //Top (y+)
              -0.5f, 0.5f,  0.5f, 0.0f, 1.0f, 0.0f,
               0.5f, 0.5f,  0.5f, 0.0f, 1.0f, 0.0f,
               0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f,
              -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f,

              //Bottom (y-)
              -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f,
               0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f,
               0.5f, -0.5f,  0.5f, 0.0f, -1.0f, 0.0f,
              -0.5f, -0.5f,  0.5f, 0.0f, -1.0f, 0.0f,

              //Front (z+)
              -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
               0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
               0.5f,  0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
              -0.5f,  0.5f, 0.5f, 0.0f, 0.0f, 1.0f,

              //Back (z-)
               0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
              -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
              -0.5f,  0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
               0.5f,  0.5f, -0.5f, 0.0f, 0.0f, -1.0f,

              //Right (x+)
              0.5f, -0.5f,  0.5f, 1.0f, 0.0f, 0.0f,
              0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
              0.5f,  0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
              0.5f,  0.5f,  0.5f, 1.0f, 0.0f, 0.0f,

              //Left (x-)
              -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f,
              -0.5f, -0.5f,  0.5f, -1.0f, 0.0f, 0.0f,
              -0.5f,  0.5f,  0.5f, -1.0f, 0.0f, 0.0f,
              -0.5f,  0.5f, -0.5f, -1.0f, 0.0f, 0.0f
            });
    ByteBuffer indices = BufferUtils.createByteBuffer(36 * 2);
    indices
        .asShortBuffer()
        .position(0)
        .put(
            new short[] {
              0, 1, 2, 2, 3, 0,
              4, 5, 6, 6, 7, 4,
              8, 9, 10, 10, 11, 8,
              12, 13, 14, 14, 15, 12,
              16, 17, 18, 18, 19, 16,
              20, 21, 22, 22, 23, 20
            });

    MESH = new IndexedMesh(
        vertices,
        PrimitiveType.TRIANGLES,
        indices,
        IndexDataType.UNSIGNED_SHORT,
        GLUsageHint.DRAW_STATIC,
        new VertexAttribute(3, DataType.FLOAT, "aPosition"),
        new VertexAttribute(3, DataType.FLOAT, "aNormal"));
  }

  /**
   * Get the color of the cuboid.
   * @return the color of the cuboid
   */
  public int color() {
    return this.color;
  }

  /**
   * Set the color of the cuboid.
   * @param rgba the new color of the cuboid in RGBA format.
   */
  public void color(int rgba) {
    this.color = rgba;
  }

  /**
   * Set the color of the cuboid.
   * @param r the red component of the color
   * @param g the green component of the color
   * @param b the blue component of the color
   * @param a the alpha component of the color
   */
  public void color(int r, int g, int b, int a) {
    this.color = (a << 24) | (r << 16) | (g << 8) | b;
  }

  /**
   * Set the color of the cuboid.
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
    Vector4f max = this.transformationMatrix().transform(new Vector4f(0.5f, 0.5f, 0.5f, 1.0f));
    Vector4f min = this.transformationMatrix().transform(new Vector4f(-0.5f, -0.5f, -0.5f, 1.0f));
    this.boundingBox = new BoundingBox(min.x, min.y, min.z, max.x, max.y, max.z);
  }

  @Override
  public boolean shouldRender(CameraFrustum frustum) {
    int frustumResult = frustum.intersectAab(this.boundingBox.getMin(), this.boundingBox.getMax());
    return frustumResult == CameraFrustum.INTERSECT || frustumResult == CameraFrustum.INSIDE;
  }
}

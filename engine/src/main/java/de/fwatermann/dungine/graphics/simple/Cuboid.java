package de.fwatermann.dungine.graphics.simple;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraFrustum;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.IndexDataType;
import de.fwatermann.dungine.graphics.mesh.IndexedMesh;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.scene.model.Material;
import de.fwatermann.dungine.graphics.scene.model.Model;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.utils.BoundingBox;
import java.nio.ByteBuffer;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

public class Cuboid extends Model {

  protected static IndexedMesh MESH;
  private BoundingBox boundingBox = new BoundingBox(0, 0, 0, 0, 0, 0);

  private int color = 0xDDDDDDFF;
  private Material material;

  /**
   * Constructs a new Cuboid with the specified position and color.
   * @param rgb the color of the cuboid in RGBA format
   */
  public Cuboid(int rgb) {
    this.color = rgb;
    initMesh();
  }

  public Cuboid() {
    this(0xDDDDDDFF);
  }

  private static void initMesh() {
    if(MESH != null) return;
    ByteBuffer vertices = BufferUtils.createByteBuffer(4 * 4 * 8 * 6);
    vertices
            .asFloatBuffer()
            .position(0)
            .put(
                    new float[] {
                            //Format: x, y, z, nx, ny, nz, u, v

                            //Top (y+)
                            -0.5f, 0.5f,  0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
                            0.5f, 0.5f,  0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f,
                            0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f,
                            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,

                            //Bottom (y-)
                            -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f,
                            0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f,
                            0.5f, -0.5f,  0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 1.0f,
                            -0.5f, -0.5f,  0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f,

                            //Front (z+)
                            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                            0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,
                            0.5f,  0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
                            -0.5f,  0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f,

                            //Back (z-)
                            0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
                            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f,
                            -0.5f,  0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f,
                            0.5f,  0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f,

                            //Right (x+)
                            0.5f, -0.5f,  0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                            0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                            0.5f,  0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
                            0.5f,  0.5f,  0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,

                            //Left (x-)
                            -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                            -0.5f, -0.5f,  0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                            -0.5f,  0.5f,  0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
                            -0.5f,  0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f
                    });
    ByteBuffer indices = BufferUtils.createByteBuffer(36 * 4);
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
            new VertexAttribute(3, DataType.FLOAT, "aNormal"),
            new VertexAttribute(2, DataType.FLOAT, "aTexCoord"));
  }

  protected void initMaterials() {
    this.material = new Material();
    this.material.diffuseColor.set(
      ((this.color >> 24) & 0xFF) / 255.0f,
      ((this.color >> 16) & 0xFF) / 255.0f,
      ((this.color >> 8) & 0xFF) / 255.0f,
      (this.color & 0xFF) / 255.0f);
    this.material.meshes.add(new Material.MeshEntry(MESH, 0, 0));
    this.materials.add(this.material);
  }

  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    if(this.materials.isEmpty())
      this.initMaterials();
    super.render(camera, shader);
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
    if(this.material != null) {
      this.material.diffuseColor.set(
        ((this.color >> 24) & 0xFF) / 255.0f,
        ((this.color >> 16) & 0xFF) / 255.0f,
        ((this.color >> 8) & 0xFF) / 255.0f,
        (this.color & 0xFF) / 255.0f);
    }
  }

  /**
   * Set the color of the cuboid.
   * @param r the red component of the color
   * @param g the green component of the color
   * @param b the blue component of the color
   * @param a the alpha component of the color
   */
  public void color(int r, int g, int b, int a) {
    this.color((a << 24) | (r << 16) | (g << 8) | b);
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

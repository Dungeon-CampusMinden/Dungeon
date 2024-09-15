package de.fwatermann.dungine.graphics.simple;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.Renderable;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraFrustum;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.IndexDataType;
import de.fwatermann.dungine.graphics.mesh.IndexedMesh;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.graphics.texture.animation.Animation;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.utils.BoundingBox;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

public class CuboidTextured extends Renderable<CuboidTextured> {

  private static ShaderProgram DEFAULT_SHADER;
  private static IndexedMesh MESH;

  private ShaderProgram shader;
  private BoundingBox boundingBox = new BoundingBox(0, 0, 0, 0, 0, 0);
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

  private static void initShader() {
    if(DEFAULT_SHADER != null) return;
    try {
      Shader vertexShader = Shader.loadShader(Resource.load("/shaders/3d/CuboidTextured.vsh"), Shader.ShaderType.VERTEX_SHADER);
      Shader fragmentShader = Shader.loadShader(Resource.load("/shaders/3d/CuboidTextured.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
      DEFAULT_SHADER = new ShaderProgram(vertexShader, fragmentShader);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load default shader for CuboidTextured", e);
    }
  }

  private static void initMesh() {
    if(MESH != null) return;

    ByteBuffer vertices = BufferUtils.createByteBuffer(4 * 6 * 6 * 4);
    vertices.asFloatBuffer().position(0).put(new float[] {
      //FRONT
      -0.5f, -0.5f, 0.5f, 0.0f, 1.0f, 0,
       0.5f, -0.5f, 0.5f, 1.0f, 1.0f, 0,
      -0.5f,  0.5f, 0.5f, 0.0f, 0.0f, 0,
       0.5f,  0.5f, 0.5f, 1.0f, 0.0f, 0,

      //BACK
       0.5f, -0.5f, -0.5f, 0.0f, 1.0f, 1,
      -0.5f, -0.5f, -0.5f, 1.0f, 1.0f, 1,
       0.5f,  0.5f, -0.5f, 0.0f, 0.0f, 1,
      -0.5f,  0.5f, -0.5f, 1.0f, 0.0f, 1,

      //LEFT
      -0.5f, -0.5f, -0.5f, 0.0f, 1.0f, 2,
      -0.5f, -0.5f,  0.5f, 1.0f, 1.0f, 2,
      -0.5f,  0.5f, -0.5f, 0.0f, 0.0f, 2,
      -0.5f,  0.5f,  0.5f, 1.0f, 0.0f, 2,

      //RIGHT
       0.5f, -0.5f,  0.5f, 0.0f, 1.0f, 3,
       0.5f, -0.5f, -0.5f, 1.0f, 1.0f, 3,
       0.5f,  0.5f,  0.5f, 0.0f, 0.0f, 3,
       0.5f,  0.5f, -0.5f, 1.0f, 0.0f, 3,

      //TOP
      -0.5f,  0.5f,  0.5f, 0.0f, 1.0f, 4,
       0.5f,  0.5f,  0.5f, 1.0f, 1.0f, 4,
      -0.5f,  0.5f, -0.5f, 0.0f, 0.0f, 4,
       0.5f,  0.5f, -0.5f, 1.0f, 0.0f, 4,

      //BOTTOM
      -0.5f, -0.5f, -0.5f, 0.0f, 1.0f, 5,
       0.5f, -0.5f, -0.5f, 1.0f, 1.0f, 5,
      -0.5f, -0.5f,  0.5f, 0.0f, 0.0f, 5,
       0.5f, -0.5f,  0.5f, 1.0f, 0.0f, 5
    });

    ByteBuffer indices = BufferUtils.createByteBuffer(6 * 6 * 2);
    indices.asShortBuffer().position(0).put(new short[] {
      0, 1, 2, 1, 3, 2,
      4, 5, 6, 5, 7, 6,
      8, 9, 10, 9, 11, 10,
      12, 13, 14, 13, 15, 14,
      16, 17, 18, 17, 19, 18,
      20, 21, 22, 21, 23, 22
    });

    MESH =
        new IndexedMesh(
            vertices,
            PrimitiveType.TRIANGLES,
            indices,
            IndexDataType.UNSIGNED_SHORT,
            GLUsageHint.DRAW_STATIC,
            new VertexAttribute(3, DataType.FLOAT, "aPosition"),
            new VertexAttribute(2, DataType.FLOAT, "aTexCoord"),
            new VertexAttribute(1, DataType.FLOAT, "aAnimationIndex"));
  }

  @Override
  public void render(Camera<?> camera) {
    if(this.shader == null) {
      initShader();
      this.render(camera, DEFAULT_SHADER);
    } else {
      this.render(camera, this.shader);
    }
  }

  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    initMesh();
    shader.bind();
    for(int i = 0; i < this.animations.length; i ++) {
      this.animations[i].bind(shader, Animation.AnimationSlot.fromIndex(i), GL33.GL_TEXTURE10 + i);
    }
    MESH.transformation(this.position(),this.rotation(), this.scaling());
    MESH.render(camera, shader);
    shader.unbind();
  }

  @Override
  public boolean shouldRender(CameraFrustum frustum) {
    int frustumResult = frustum.intersectAab(this.boundingBox.getMin(), this.boundingBox.getMax());
    return frustumResult == CameraFrustum.INTERSECT || frustumResult == CameraFrustum.INSIDE;
  }

  @Override
  protected void transformationChanged() {
    super.transformationChanged();
    if(MESH == null) return;
    this.boundingBox = BoundingBox.fromVertices(MESH.vertexBuffer().asFloatBuffer(), 0, 5, 24, this.transformationMatrix());
  }
}

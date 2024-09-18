package de.fwatermann.dungine.graphics;

import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraFrustum;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.IndexDataType;
import de.fwatermann.dungine.graphics.mesh.IndexedMesh;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.graphics.texture.Texture;
import de.fwatermann.dungine.graphics.texture.TextureManager;
import de.fwatermann.dungine.resource.Resource;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SkyBox extends Renderable<SkyBox> {

  private static ShaderProgram DEFAULT_SHADER;
  private static IndexedMesh MESH;

  private ShaderProgram shader;
  private Texture texture;

  public SkyBox(Resource cubeMap) {
    this.texture = TextureManager.load(cubeMap);
  }

  public SkyBox(Texture texture) {
    this.texture = texture;
  }

  public SkyBox() {
    this.texture = TextureManager.load(Resource.load("/textures/CubeMap.png"));
  }

  private static void initShader() {
    if(DEFAULT_SHADER != null) return;
    try {
      Shader vertexShader = Shader.loadShader(Resource.load("/shaders/3d/SkyBox.vsh"), Shader.ShaderType.VERTEX_SHADER);
      Shader fragmentShader = Shader.loadShader(Resource.load("/shaders/3d/SkyBox.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
      DEFAULT_SHADER = new ShaderProgram(vertexShader, fragmentShader);
    } catch(IOException ex) {
      throw new RuntimeException("Could not load default shader for SkyBox!", ex);
    }
  }

  private static void initMesh() {
    if(MESH != null) return;

    ByteBuffer vertices = BufferUtils.createByteBuffer(6 * 4 * 5 * 4);

    float oT = 1.0f / 3.0f;
    float tT = 2.0f / 3.0f;

    vertices.asFloatBuffer().position(0).put(new float[] {
      //nZ
      -0.5f, 0.5f, -0.5f, 0.25f, oT,
      -0.5f, -0.5f, -0.5f, 0.25f, tT,
      0.5f, -0.5f, -0.5f, 0.5f, tT,
      0.5f, 0.5f, -0.5f, 0.5f, oT,

      //pZ
      0.5f, -0.5f, 0.5f, 0.75f, tT,
      -0.5f, -0.5f, 0.5f, 1.0f, tT,
      -0.5f, 0.5f, 0.5f, 1.0f, oT,
      0.5f, 0.5f, 0.5f, 0.75f, oT,

      //pY
      -0.5f, 0.5f, -0.5f, 0.25f, oT,
      0.5f, 0.5f, -0.5f, 0.5f, oT,
      0.5f, 0.5f, 0.5f, 0.5f, 0.0f,
      -0.5f, 0.5f, 0.5f, 0.25f, 0.0f,

      //nY
      -0.5f, -0.5f, 0.5f, 0.25f, 1.0f,
      0.5f, -0.5f, 0.5f, 0.5f, 1.0f,
      0.5f, -0.5f, -0.5f, 0.5f, tT,
      -0.5f, -0.5f, -0.5f, 0.25f, tT,

      //pX
      0.5f, -0.5f, -0.5f, 0.5f, tT,
      0.5f, -0.5f, 0.5f, 0.75f, tT,
      0.5f, 0.5f, 0.5f, 0.75f, oT,
      0.5f, 0.5f, -0.5f, 0.5f, oT,

      //nX
      -0.5f, -0.5f, 0.5f, 0.0f, tT,
      -0.5f, -0.5f, -0.5f, 0.25f, tT,
      -0.5f, 0.5f, -0.5f, 0.25f, oT,
      -0.5f, 0.5f, 0.5f, 0.0f, oT,
    });

    ByteBuffer indices = BufferUtils.createByteBuffer(6 * 6 * 2);
    indices.asShortBuffer().position(0).put(new short[] {
      0, 1, 2, 2, 3, 0,
      4, 5, 6, 6, 7, 4,
      8, 9, 10, 10, 11, 8,
      12, 13, 14, 14, 15, 12,
      16, 17, 18, 18, 19, 16,
      20, 21, 22, 22, 23, 20
    });

    MESH = new IndexedMesh(vertices, PrimitiveType.TRIANGLES, indices, IndexDataType.UNSIGNED_SHORT, GLUsageHint.DRAW_STATIC,
      new VertexAttribute(3, DataType.FLOAT, "aPosition"),
      new VertexAttribute(2, DataType.FLOAT, "aTexCoord"));
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
    shader.useCamera(camera);
    this.texture.bind(GL33.GL_TEXTURE0);
    shader.setUniform1i("uCubeMap", 0);
    MESH.transformation(camera.position(), new Quaternionf(), new Vector3f(100.0f));
    MESH.render(camera, shader);
    shader.unbind();
  }

  @Override
  public boolean shouldRender(CameraFrustum frustum) {
    return true;
  }
}

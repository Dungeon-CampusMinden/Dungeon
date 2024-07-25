package de.fwatermann.dungine.graphics.simple;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.IRenderable;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.mesh.ArrayMesh;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.graphics.texture.Texture;
import de.fwatermann.dungine.graphics.texture.TextureManager;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.utils.ThreadUtils;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

public class Sprite implements IRenderable {

  public enum BillboardMode {
    NONE(0),
    SPHERICAL(1),
    CYLINDRICAL(2);
    public final int value;

    BillboardMode(int value) {
      this.value = value;
    }
  }

  private static ShaderProgram SHADER;
  private static ArrayMesh MESH;

  public BillboardMode billboardMode;
  private float width, height;
  private Texture texture;

  public Sprite(Resource textureResource, float width, float height, BillboardMode billboardMode) {
    this.texture = TextureManager.instance().load(textureResource);
    this.width = width;
    this.height = height;
    this.billboardMode = billboardMode;
  }

  @Override
  public void render(Camera<?> camera) {
    if (SHADER == null) {
      Shader vertexShader = new Shader(VERTEX_SHADER, Shader.ShaderType.VERTEX_SHADER);
      Shader fragmentShader = new Shader(FRAGMENT_SHADER, Shader.ShaderType.FRAGMENT_SHADER);
      SHADER = new ShaderProgram(vertexShader, fragmentShader);
    }
    this.render(camera, SHADER);
  }

  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    if (MESH == null) {
      initMesh();
    }
    shader.bind();
    this.texture.bind(GL33.GL_TEXTURE0);
    shader.setUniform1i("uTexture", 0);
    shader.setUniform1i("uBillboardMode", this.billboardMode.value);
    MESH.setScale(this.width, this.height, 1.0f);
    MESH.render(camera, shader);
    shader.unbind();
  }

  public void texture(Resource resource) {
    this.texture = TextureManager.instance().load(resource);
  }

  public float width() {
    return this.width;
  }

  public Sprite width(float width) {
    this.width = width;
    return this;
  }

  public float height() {
    return this.height;
  }

  public Sprite height(float height) {
    this.height = height;
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
              -0.5f, -0.5f, 0.0f, 0.0f, 0.0f,
              +0.5f, -0.5f, 0.0f, 1.0f, 0.0f,
              -0.5f, +0.5f, 0.0f, 0.0f, 1.0f,
              +0.5f, +0.5f, 0.0f, 1.0f, 1.0f
            });
    MESH =
        new ArrayMesh(
            vertices,
            PrimitiveType.TRIANGLE_STRIP,
            GLUsageHint.DRAW_STATIC,
            new VertexAttribute(3, DataType.FLOAT, "a_Position"),
            new VertexAttribute(2, DataType.FLOAT, "a_TexCoord"));
  }

  private static final String VERTEX_SHADER =
      """
#version 330 core

in vec3 a_Position;
in vec2 a_TexCoord;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

uniform int uBillboardMode;

out vec2 v_TexCoord;

void main() {
  mat4 modelView = uView * uModel;
  if (uBillboardMode == 1) {
    modelView[0][0] = 1.0;
    modelView[0][1] = 0.0;
    modelView[0][2] = 0.0;
    modelView[1][0] = 0.0;
    modelView[1][1] = 1.0;
    modelView[1][2] = 0.0;
    modelView[2][0] = 0.0;
    modelView[2][1] = 0.0;
    modelView[2][2] = 1.0;
    gl_Position = uProjection * modelView * vec4(a_Position, 1.0);
  } else if (uBillboardMode == 2) {
    modelView[0][0] = 1.0;
    modelView[0][1] = 0.0;
    modelView[0][2] = 0.0;
    modelView[2][0] = 0.0;
    modelView[2][1] = 0.0;
    modelView[2][2] = 1.0;
    gl_Position = uProjection * modelView * vec4(a_Position, 1.0);
  } else {
    gl_Position = uProjection * uView * uModel * vec4(a_Position, 1.0);
  }
  v_TexCoord = a_TexCoord;
}
""";

  private static final String FRAGMENT_SHADER =
      """
#version 330 core

in vec2 v_TexCoord;

uniform sampler2D uTexture;

out vec4 fragColor;

void main() {
  fragColor = texture(uTexture, v_TexCoord);
}
""";
}

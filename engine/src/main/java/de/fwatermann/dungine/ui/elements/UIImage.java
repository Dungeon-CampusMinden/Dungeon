package de.fwatermann.dungine.ui.elements;

import de.fwatermann.dungine.graphics.GLUsageHint;
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
import de.fwatermann.dungine.ui.UIElement;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

public class UIImage extends UIElement<UIImage> {

  private static ShaderProgram SHADER;
  private static ArrayMesh MESH;

  private static void initShader() {
    if (SHADER != null) return;
    Shader vertexShader = new Shader(VERTEX_SHADER, Shader.ShaderType.VERTEX_SHADER);
    Shader fragmentShader = new Shader(FRAGMENT_SHADER, Shader.ShaderType.FRAGMENT_SHADER);
    SHADER = new ShaderProgram(vertexShader, fragmentShader);
  }

  private static void initMesh() {
    if (MESH != null) return;
    ByteBuffer vertices = BufferUtils.createByteBuffer(6 * 5 * 4);
    vertices
        .asFloatBuffer()
        .put(
            new float[] {
              0.0f, 0.0f, 0.0f, 0.0f, 1.0f,
              1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
              1.0f, 1.0f, 0.0f, 1.0f, 0.0f,
              1.0f, 1.0f, 0.0f, 1.0f, 0.0f,
              0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
              0.0f, 0.0f, 0.0f, 0.0f, 1.0f
            })
        .flip();
    MESH =
        new ArrayMesh(
            vertices,
            PrimitiveType.TRIANGLES,
            GLUsageHint.DRAW_STATIC,
            new VertexAttribute(3, DataType.FLOAT, "aPosition"),
            new VertexAttribute(2, DataType.FLOAT, "aTexCoord"));
  }

  private Texture texture;

  public UIImage(Resource image) {
    this.texture = TextureManager.load(image);
  }

  public UIImage texture(Resource resource) {
    this.texture = TextureManager.load(resource);
    return this;
  }

  @Override
  protected void render(Camera<?> camera) {
    initShader();
    initMesh();

    MESH.transformation(this.position, this.rotation, this.size);
    SHADER.bind();
    this.texture.bind(GL33.GL_TEXTURE0);
    SHADER.setUniform1i("uTexture", 0);
    MESH.render(camera, SHADER);
    SHADER.unbind();
    this.texture.unbind();
  }

  private static final String VERTEX_SHADER =
      """
#version 330 core

layout (location=0) in vec3 aPosition;
layout (location=1) in vec2 aTexCoord;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;

out vec2 vTexCoord;

void main() {
  gl_Position = uProjection * uView * uModel * vec4(aPosition, 1.0);
  vTexCoord = aTexCoord;
}

""";

  private static final String FRAGMENT_SHADER =
      """
#version 330 core

in vec2 vTexCoord;

uniform sampler2D uTexture;;

out vec4 fragColor;

void main() {
  fragColor = texture(uTexture, vTexCoord);
}
""";
}

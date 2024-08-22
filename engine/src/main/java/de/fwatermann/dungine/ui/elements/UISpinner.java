package de.fwatermann.dungine.ui.elements;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.mesh.ArrayMesh;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.ui.UIElement;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

public class UISpinner extends UIElement<UISpinner> {

  private static ShaderProgram SHADER;
  private static ArrayMesh MESH;

  private int color = 0xFFFFFFFF;

  public UISpinner() {
    super();
  }

  private static void initGL() {

    if(SHADER == null) {
      try {
        Shader vertexShader = Shader.loadShader(Resource.load("/shaders/ui/Spinner.vsh"), Shader.ShaderType.VERTEX_SHADER);
        Shader fragmentShader = Shader.loadShader(Resource.load("/shaders/ui/Spinner.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
        SHADER = new ShaderProgram(vertexShader, fragmentShader);
      } catch(IOException ex) {
        throw new RuntimeException(ex);
      }
    }

    if (MESH == null) {
      ByteBuffer vertices = BufferUtils.createByteBuffer(6 * 3 * 4);
      vertices
        .asFloatBuffer()
        .put(
          new float[] {
            0.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f
          })
        .flip();

      MESH =
        new ArrayMesh(
          vertices,
          PrimitiveType.TRIANGLES,
          GLUsageHint.DRAW_STATIC,
          new VertexAttribute(3, DataType.FLOAT, "aPosition"));
    }
  }

  @Override
  protected void render(Camera<?> camera) {
    initGL();

    MESH.transformation(this.absolutePosition(), this.rotation, this.size);
    SHADER.bind();
    SHADER.setUniform1i("uColor", this.color);
    SHADER.setUniform1i("uTime", (int) System.currentTimeMillis());
    SHADER.setUniform1f("uThickness", 0.1f);
    MESH.render(camera, SHADER);
    SHADER.unbind();
  }

  public int color() {
    return this.color;
  }

  public UISpinner color(int color) {
    this.color = color;
    return this;
  }
}

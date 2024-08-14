package de.fwatermann.dungine.ui.elements;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.mesh.ArrayMesh;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.ui.UIElement;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class UIText extends UIElement<UIText> {

  private static ShaderProgram SHADER;

  private Font font;
  private String text;
  private int fontSize = 16;

  private Font.TextLayoutElement[] layoutElements;
  private ArrayMesh mesh;
  private final List<RenderStep> renderSteps = new ArrayList<>();

  public UIText(Font font, String text) {
    this.font = font;
    this.text = text;
  }

  private static void initShader() {
    if(SHADER != null) return;
    try {
      Shader vertexShader = Shader.loadShader(Resource.load("/shaders/UIText.vsh"), Shader.ShaderType.VERTEX_SHADER);
      Shader geometryShader = Shader.loadShader(Resource.load("/shaders/UIText.gsh"), Shader.ShaderType.GEOMETRY_SHADER);
      Shader fragmentShader = Shader.loadShader(Resource.load("/shaders/UIText.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
      SHADER = new ShaderProgram(vertexShader, geometryShader, fragmentShader);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void initMesh() {
    if(this.mesh != null) return;
    this.mesh =
        new ArrayMesh(
            null,
            PrimitiveType.POINTS,
            GLUsageHint.DRAW_STATIC,
            new VertexAttribute(3, DataType.FLOAT, "a_Position"), // 12
            new VertexAttribute(2, DataType.INT, "a_TexCoord"), // 8
            new VertexAttribute(2, DataType.INT, "a_Size")); // 8
    this.update();
  }

  @Override
  protected void render(Camera<?> camera) {
    initShader();
    this.initMesh();

    SHADER.bind();
    SHADER.useCamera(camera);
    SHADER.setUniform4fv("uColor", 1.0f, 1.0f, 1.0f, 1.0f);
    SHADER.setUniform2iv("uPageSize", Font.PAGE_SIZE_X, Font.PAGE_SIZE_Y);
    SHADER.setUniform1i("uPage", 0);
    this.renderSteps.forEach(
        s -> {
          GL33.glActiveTexture(GL33.GL_TEXTURE0);
          GL33.glBindTexture(GL33.GL_TEXTURE_2D, s.pageGLHandle);
          this.mesh.render(camera, SHADER, s.offset, s.count);
        });
    SHADER.unbind();
  }

  private void update() {
    if(this.mesh == null) return;

    this.layoutElements = this.font.layoutText(this.text, this.fontSize, (int) this.size.x);
    this.renderSteps.clear();
    Arrays.sort(this.layoutElements, Comparator.comparingInt(a -> a != null ? a.glyph.page : 0));

    ByteBuffer buffer = BufferUtils.createByteBuffer(this.layoutElements.length * 28);
    int count = 0;
    int offset = 0;
    int page = 0;
    for(int i = 0; i < this.layoutElements.length; i++) {
      Font.TextLayoutElement element = this.layoutElements[i];
      if(element == null) {
        continue;
      }
      if(element.glyph.page != page) {
        this.renderSteps.add(new RenderStep(this.font.getPage(page).glHandle(), count, offset));
        offset = count;
        count = 0;
        page = element.glyph.page;
      }

      buffer.putFloat(this.position.x + element.x);
      buffer.putFloat(this.position.y + element.y);
      buffer.putFloat(this.position.z);
      buffer.putInt(element.glyph.pageX);
      buffer.putInt(element.glyph.pageY);
      buffer.putInt(element.glyph.width);
      buffer.putInt(element.glyph.height);

      count++;
    }
    buffer.flip();
    this.renderSteps.add(new RenderStep(this.font.getPage(page).glHandle(), count, offset));
    this.mesh.vertexBuffer(buffer);
  }

  public void text(String text) {
    this.text = text;
    this.update();
  }

  public String text() {
    return this.text;
  }

  public Font font() {
    return this.font;
  }

  public UIText font(Font font) {
    this.font = font;
    this.update();
    return this;
  }

  public int fontSize() {
    return this.fontSize;
  }

  public UIText fontSize(int fontSize) {
    this.fontSize = fontSize;
    this.update();
    return this;
  }

  private record RenderStep(int pageGLHandle, int count, int offset) {}

}

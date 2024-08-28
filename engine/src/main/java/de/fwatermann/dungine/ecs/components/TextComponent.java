package de.fwatermann.dungine.ecs.components;

import de.fwatermann.dungine.ecs.Component;
import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.graphics.BillboardMode;
import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.mesh.ArrayMesh;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.graphics.text.TextAlignment;
import de.fwatermann.dungine.resource.Resource;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

public class TextComponent extends Component {

  private static final float FONT_SIZE_FACTOR = 400f;

  private static ShaderProgram DEFAULT_SHADER;

  private final List<RenderStep> renderSteps = new ArrayList<>();

  private Vector3f offset;
  private BillboardMode billboardMode;
  private Font.TextLayoutElement[] layoutElements;
  private Font font = Font.defaultMonoFont();
  private ShaderProgram shader;
  private ArrayMesh mesh;
  private String text;
  private Vector3f size = new Vector3f(1.0f);
  private TextAlignment alignment = TextAlignment.LEFT;
  private int fontSize = 24;
  private int backgroundColor = 0x00000000;

  public TextComponent(Vector3f offset, String text, BillboardMode billboardMode) {
    super(true);
    this.offset = offset;
    this.text = text;
    this.billboardMode = billboardMode;
  }

  public Vector3f offset() {
    return this.offset;
  }

  public void render(Camera<?> camera, Entity attachedEntity) {
    if(this.shader == null && DEFAULT_SHADER == null) {
      try {
        Shader vertexShader = Shader.loadShader(Resource.load("/shaders/3d/3DText.vsh"), Shader.ShaderType.VERTEX_SHADER);
        Shader geometryShader = Shader.loadShader(Resource.load("/shaders/3d/3DText.gsh"), Shader.ShaderType.GEOMETRY_SHADER);
        Shader fragmentShader = Shader.loadShader(Resource.load("/shaders/3d/3DText.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
        DEFAULT_SHADER = new ShaderProgram(vertexShader, geometryShader, fragmentShader);
      } catch(IOException ex) {
        throw new RuntimeException("Failed to load default shader for TextComponent", ex);
      }
    }
    this.render(camera, this.shader != null ? this.shader : DEFAULT_SHADER, attachedEntity);
  }

  public void render(Camera<?> camera, ShaderProgram shader, Entity attachedEntity) {
    this.initMesh();
    shader.bind();
    shader.useCamera(camera);
    shader.setUniform4fv("uColor", 1.0f, 1.0f, 1.0f, 1.0f);
    shader.setUniform2iv("uPageSize", Font.PAGE_SIZE_X, Font.PAGE_SIZE_Y);
    shader.setUniform1i("uPage", 0);
    shader.setUniform1i("uBillboardMode", this.billboardMode.value);
    shader.setUniform3f("uBasePosition", this.offset);
    shader.setUniform3f("uScale", new Vector3f(1.0f / FONT_SIZE_FACTOR));
    this.mesh.transformation(attachedEntity.position(), attachedEntity.rotation(), new Vector3f(1.0f / FONT_SIZE_FACTOR));
    this.renderSteps.forEach(step -> {
      GL33.glActiveTexture(GL33.GL_TEXTURE0);
      GL33.glBindTexture(GL33.GL_TEXTURE_2D, step.pageGLHandle);
      this.mesh.render(camera, shader, step.offset, step.count);
    });
    shader.unbind();
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
    this.updateMesh();
  }

  private void updateMesh() {
    if(this.mesh == null) return;
    this.layoutElements = this.font.layoutText(this.text, this.fontSize, Math.round(this.size.x * FONT_SIZE_FACTOR), this.alignment);
    this.renderSteps.clear();
    Arrays.sort(this.layoutElements, Comparator.comparingInt(a -> a != null ? a.glyph.page : 0));

    ByteBuffer buffer = BufferUtils.createByteBuffer(this.layoutElements.length * 28);
    int count = 0;
    int offset = 0;
    int page = 0;
    for (int i = 0; i < this.layoutElements.length; i++) {
      Font.TextLayoutElement element = this.layoutElements[i];
      if (element == null) {
        continue;
      }
      if (element.glyph.page != page) {
        this.renderSteps.add(new RenderStep(this.font.getPage(page).glHandle(), count, offset));
        offset = count;
        count = 0;
        page = element.glyph.page;
      }

      buffer.putFloat(element.x);
      buffer.putFloat(element.y);
      buffer.putFloat(0.0f);
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

  public TextComponent offset(Vector3f offset) {
    this.offset = offset;
    return this;
  }

  public BillboardMode billboardMode() {
    return this.billboardMode;
  }

  public TextComponent billboardMode(BillboardMode billboardMode) {
    this.billboardMode = billboardMode;
    return this;
  }

  public Font font() {
    return this.font;
  }

  public TextComponent font(Font font) {
    if(this.font != font) {
      this.font = font;
      this.updateMesh();
    }
    return this;
  }

  public ShaderProgram shader() {
    return this.shader;
  }

  public TextComponent shader(ShaderProgram shader) {
    this.shader = shader;
    return this;
  }

  public String text() {
    return this.text;
  }

  public TextComponent text(String text) {
    if(!this.text.equals(text)) {
      this.text = text;
      this.updateMesh();
    }
    return this;
  }

  public int fontSize() {
    return this.fontSize;
  }

  public TextComponent fontSize(int fontSize) {
    if(this.fontSize != fontSize) {
      this.fontSize = fontSize;
      this.updateMesh();
    }
    return this;
  }

  public Vector3f size() {
    return this.size;
  }

  public TextComponent size(Vector3f size) {
    if(!this.size.equals(size)) {
      this.size = size;
      this.updateMesh();
    }
    return this;
  }

  public int backgroundColor() {
    return this.backgroundColor;
  }

  public TextComponent backgroundColor(int backgroundColor) {
    this.backgroundColor = backgroundColor;
    return this;
  }

  public TextAlignment alignment() {
    return this.alignment;
  }

  public TextComponent alignment(TextAlignment alignment) {
    if(this.alignment != alignment) {
      this.alignment = alignment;
      this.updateMesh();
    }
    return this;
  }

  /** Represents a render step for a specific texture page. */
  private record RenderStep(int pageGLHandle, int count, int offset) {}
}

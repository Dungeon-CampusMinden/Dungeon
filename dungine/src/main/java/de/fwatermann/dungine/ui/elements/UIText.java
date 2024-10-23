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
import de.fwatermann.dungine.graphics.text.TextAlignment;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.ui.UIElement;
import de.fwatermann.dungine.ui.layout.Unit;
import de.fwatermann.dungine.utils.BoundingBox2D;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

/** Represents a UI element for rendering text. */
public class UIText extends UIElement<UIText> {

  /** Whether to render debug bounding boxes. */
  public static boolean debug = false;

  private static ShaderProgram SHADER;

  private Font font;
  private String text;
  private int fontSize = 24;

  private Font.TextLayoutElement[] layoutElements;
  private ArrayMesh mesh;
  private TextAlignment alignment;
  private final List<RenderStep> renderSteps = new ArrayList<>();

  private UIColorPane boundingBox = new UIColorPane(0x00FF00FF, 0xFFFFFFFF, 0, 0);

  /**
   * Constructs a new UIText object.
   *
   * @param font the font to use for rendering the text
   * @param text the text to render
   * @param fontSize the font size to use for rendering the text
   * @param alignment the text alignment to use for rendering the text
   */
  public UIText(Font font, String text, int fontSize, TextAlignment alignment) {
    this.font = font;
    this.text = text;
    this.fontSize = fontSize;
    this.alignment = alignment;
    this.layout.flexGrow(0).height(Unit.px(fontSize));
  }

  /**
   * Constructs a new UIText object.
   *
   * @param font the font to use for rendering the text
   * @param text the text to render
   * @param fontSize the font size to use for rendering the text
   */
  public UIText(Font font, String text, int fontSize) {
    this(font, text, fontSize, TextAlignment.LEFT);
  }

  /**
   * Constructs a new UIText object.
   *
   * @param font the font to use for rendering the text
   * @param text the text to render
   */
  public UIText(Font font, String text) {
    this(font, text, 24);
  }

  /**
   * Initializes the shader program for rendering UIText elements.
   *
   * <p>This method loads the vertex, geometry, and fragment shaders from the specified resources
   * and creates a ShaderProgram object. If the shader program is already initialized, it does
   * nothing.
   *
   * @throws RuntimeException if there is an error loading the shaders
   */
  private static void initShader() {
    if (SHADER != null) return;
    try {
      Shader vertexShader =
          Shader.loadShader(Resource.load("/shaders/ui/Text.vsh"), Shader.ShaderType.VERTEX_SHADER);
      Shader geometryShader =
          Shader.loadShader(
              Resource.load("/shaders/ui/Text.gsh"), Shader.ShaderType.GEOMETRY_SHADER);
      Shader fragmentShader =
          Shader.loadShader(
              Resource.load("/shaders/ui/Text.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
      SHADER = new ShaderProgram(vertexShader, geometryShader, fragmentShader);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Initializes the mesh for rendering UIText elements.
   *
   * <p>This method creates a new ArrayMesh object with the specified vertex attributes and updates
   * the mesh with the current text layout. If the mesh is already initialized, it does nothing.
   */
  private void initMesh() {
    if (this.mesh != null) return;
    this.mesh =
        new ArrayMesh(
            null,
            PrimitiveType.POINTS,
            GLUsageHint.DRAW_STATIC,
            new VertexAttribute(3, DataType.FLOAT, "a_Position"), // 12
            new VertexAttribute(2, DataType.INT, "a_TexCoord"), // 8
            new VertexAttribute(2, DataType.INT, "a_Size")); // 8
    this.updateTextElements();
  }

  /**
   * Renders the UIText element using the specified camera.
   *
   * <p>This method binds the shader program, sets the necessary uniforms, and renders the mesh for
   * each render step. It ensures that the shader program and mesh are initialized before rendering.
   *
   * @param camera the camera to use for rendering
   */
  @Override
  protected void render(Camera<?> camera) {
    initShader();
    this.initMesh();

    // TODO: Add transformation of UIElement to UIText

    Vector3f basePos = new Vector3f(this.absolutePosition());
    basePos.add(0.0f, this.size.y() - this.fontSize, 0.0f);

    if (debug) {
      this.boundingBox.position().set(this.absolutePosition());
      this.boundingBox.size().set(this.size);
      this.boundingBox.render(camera);
    }

    SHADER.bind();
    SHADER.useCamera(camera);
    SHADER.setUniform4fv("uColor", 1.0f, 1.0f, 1.0f, 1.0f);
    SHADER.setUniform2iv("uPageSize", Font.PAGE_SIZE_X, Font.PAGE_SIZE_Y);
    SHADER.setUniform1i("uPage", 0);
    SHADER.setUniform3f("uBasePosition", basePos);
    this.renderSteps.forEach(
        s -> {
          GL33.glActiveTexture(GL33.GL_TEXTURE0);
          GL33.glBindTexture(GL33.GL_TEXTURE_2D, s.pageGLHandle);
          this.mesh.render(camera, SHADER, s.offset, s.count);
        });
    SHADER.unbind();
  }

  /**
   * Updates the mesh with the current text layout.
   *
   * <p>This method lays out the text using the specified font and font size, sorts the layout
   * elements by page, and updates the vertex buffer with the layout data. It also updates the
   * render steps for each page.
   */
  private void updateTextElements() {
    if (this.mesh == null) return;

    this.layoutElements =
        this.font.layoutText(this.text, this.fontSize, (int) this.size.x, this.alignment);
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
    BoundingBox2D bb = this.font.calculateBoundingBox(this.layoutElements);
    if (this.layout.height().type() == Unit.UnitType.AUTO) {
      this.size.setComponent(1, bb.height());
    }
  }

  /**
   * Sets the text to display.
   *
   * @param text the new text
   */
  public void text(String text) {
    if (this.text.equals(text)) return;
    this.text = text;
    this.updateTextElements();
  }

  /**
   * Returns the text displayed by this element.
   *
   * @return the text displayed by this element
   */
  public String text() {
    return this.text;
  }

  /**
   * Returns the font used by this element.
   *
   * @return the font used by this element
   */
  public Font font() {
    return this.font;
  }

  /**
   * Sets the font used by this element.
   *
   * @param font the new font
   * @return this element
   */
  public UIText font(Font font) {
    if (this.font.equals(font)) return this;
    this.font = font;
    this.updateTextElements();
    return this;
  }

  /**
   * Returns the font size used by this element.
   *
   * @return the font size used by this element
   */
  public int fontSize() {
    return this.fontSize;
  }

  /**
   * Sets the font size used by this element.
   *
   * @param fontSize the new font size
   * @return this element
   */
  public UIText fontSize(int fontSize) {
    if (this.fontSize == fontSize) return this;
    this.fontSize = fontSize;
    this.updateTextElements();
    return this;
  }

  /**
   * Returns the text alignment used by this element.
   *
   * @return the text alignment.
   */
  public TextAlignment alignment() {
    return this.alignment;
  }

  /**
   * Sets the text alignment used by this element.
   *
   * @param alignment the new text alignment
   * @return this element
   */
  public UIText alignment(TextAlignment alignment) {
    if (this.alignment == alignment) return this;
    this.alignment = alignment;
    this.updateTextElements();
    return this;
  }

  /**
   * Represents a render step for a specific texture page.
   *
   * @param pageGLHandle the OpenGL handle of the texture page
   * @param count the number of elements to render
   * @param offset the offset of the first element to render
   */
  private record RenderStep(int pageGLHandle, int count, int offset) {}

  @Override
  protected void update() {
    this.updateTextElements();
  }
}

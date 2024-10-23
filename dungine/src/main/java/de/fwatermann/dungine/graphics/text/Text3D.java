package de.fwatermann.dungine.graphics.text;

import de.fwatermann.dungine.graphics.BillboardMode;
import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.Renderable;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraFrustum;
import de.fwatermann.dungine.graphics.mesh.ArrayMesh;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.utils.BoundingBox2D;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

/** Represents a 3D text object that can be rendered in a 3D space. */
public class Text3D extends Renderable<Text3D> {

  private static final float FONT_SIZE_FACTOR = 400f;
  private static ShaderProgram DEFAULT_SHADER;

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
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
  private OriginMode originMode = OriginMode.TOP_LEFT;
  private int fontSize = 24;
  private int backgroundColor = 0x00000000;

  /**
   * Constructs a new Text3D object with the specified parameters.
   *
   * @param text the text to display
   * @param offset the offset position of the text
   * @param originMode the origin mode of the text
   * @param textAlignment the alignment of the text
   * @param billboardMode the billboard mode of the text
   */
  public Text3D(
      String text,
      Vector3f offset,
      OriginMode originMode,
      TextAlignment textAlignment,
      BillboardMode billboardMode) {
    this.text = text;
    this.offset = offset;
    this.originMode = originMode;
    this.alignment = textAlignment;
    this.billboardMode = billboardMode;
  }

  /**
   * Constructs a new Text3D object with the specified parameters.
   *
   * @param text the text to display
   * @param textAlignment the alignment of the text
   * @param billboardMode the billboard mode of the text
   */
  public Text3D(String text, TextAlignment textAlignment, BillboardMode billboardMode) {
    this(text, new Vector3f(0), OriginMode.CENTER, textAlignment, billboardMode);
  }

  /**
   * Constructs a new Text3D object with the specified parameters.
   *
   * @param text the text to display
   * @param billboardMode the billboard mode of the text
   */
  public Text3D(String text, BillboardMode billboardMode) {
    this(text, new Vector3f(0), OriginMode.CENTER, TextAlignment.CENTER, BillboardMode.NONE);
  }

  /**
   * Constructs a new Text3D object with the specified parameters.
   *
   * @param text the text to display
   * @param textAlignment the alignment of the text
   */
  public Text3D(String text, TextAlignment textAlignment) {
    this(text, new Vector3f(0), OriginMode.CENTER, textAlignment, BillboardMode.NONE);
  }

  /**
   * Constructs a new Text3D object with the specified text.
   *
   * @param text the text to display
   */
  public Text3D(String text) {
    this(text, new Vector3f(0), OriginMode.CENTER, TextAlignment.CENTER, BillboardMode.NONE);
  }

  private static void initShader() {
    if (DEFAULT_SHADER != null) return;
    try {
      Shader vertexShader =
          Shader.loadShader(
              Resource.load("/shaders/3d/3DText.vsh"), Shader.ShaderType.VERTEX_SHADER);
      Shader geometryShader =
          Shader.loadShader(
              Resource.load("/shaders/3d/3DText.gsh"), Shader.ShaderType.GEOMETRY_SHADER);
      Shader fragmentShader =
          Shader.loadShader(
              Resource.load("/shaders/3d/3DText.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
      DEFAULT_SHADER = new ShaderProgram(vertexShader, geometryShader, fragmentShader);
    } catch (IOException ex) {
      throw new RuntimeException("Failed to load default shader for Text3D!", ex);
    }
  }

  private void initMesh() {
    if (this.mesh != null) return;
    this.mesh =
        new ArrayMesh(
            null,
            PrimitiveType.POINTS,
            GLUsageHint.DRAW_STATIC,
            new VertexAttribute(3, DataType.FLOAT, "aPosition"),
            new VertexAttribute(2, DataType.FLOAT, "aTexCoord"),
            new VertexAttribute(2, DataType.FLOAT, "aSize"));
    this.updateMesh();
  }

  private void updateMesh() {
    if (this.mesh == null) return;
    this.layoutElements =
        this.font.layoutText(
            this.text, this.fontSize, Math.round(this.size.x * FONT_SIZE_FACTOR), this.alignment);
    BoundingBox2D bounds =
        this.font.calculateBoundingBox(
            this.text, this.fontSize, Math.round(this.size.x * FONT_SIZE_FACTOR));
    Vector3f posOffset = new Vector3f(0, 0, 0);
    if (this.originMode == OriginMode.CENTER) {
      posOffset.set(-bounds.width() / 2.0f, -bounds.height() / 2.0f, 0);
    }

    this.lock.writeLock().lock();
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

      buffer.putFloat(element.x + posOffset.x);
      buffer.putFloat(element.y + posOffset.y);
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
    this.lock.writeLock().unlock();
  }

  /**
   * Renders the text using the specified camera.
   *
   * @param camera the camera to use for rendering
   */
  @Override
  public void render(Camera<?> camera) {
    initShader();
    this.render(camera, this.shader != null ? this.shader : DEFAULT_SHADER);
  }

  /**
   * Renders the text using the specified camera and shader program.
   *
   * @param camera the camera to use for rendering
   * @param shader the shader program to use for rendering
   */
  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    this.initMesh();

    shader.bind();
    shader.useCamera(camera);
    shader.setUniform4fv("uColor", 1.0f, 1.0f, 1.0f, 1.0f);
    shader.setUniform2iv("uPageSize", Font.PAGE_SIZE_X, Font.PAGE_SIZE_Y);
    shader.setUniform1i("uPage", 0);
    shader.setUniform1i("uBillboardMode", this.billboardMode.value);
    shader.setUniform3f("uBasePosition", this.offset);

    this.mesh.transformation(
        this.position(), this.rotation(), this.scaling().div(FONT_SIZE_FACTOR, new Vector3f()));
    this.lock.readLock().lock();
    this.renderSteps.forEach(
        step -> {
          GL33.glActiveTexture(GL33.GL_TEXTURE0);
          GL33.glBindTexture(GL33.GL_TEXTURE_2D, step.pageGLHandle);
          this.mesh.render(camera, shader, step.offset, step.count);
        });
    this.lock.readLock().unlock();
    shader.unbind();
  }

  /**
   * Determines whether the text should be rendered based on the camera frustum.
   *
   * @param frustum the camera frustum
   * @return true if the text should be rendered, false otherwise
   */
  @Override
  public boolean shouldRender(CameraFrustum frustum) {
    return true;
    // TODO: Implement frustum culling for 3DText.
  }

  /**
   * Gets the origin mode of the text.
   *
   * @return the origin mode
   */
  public OriginMode originMode() {
    return this.originMode;
  }

  /**
   * Sets the origin mode of the text.
   *
   * @param originMode the origin mode to set
   * @return the updated Text3D object
   */
  public Text3D originMode(OriginMode originMode) {
    this.originMode = originMode;
    return this;
  }

  /**
   * Gets the offset position of the text.
   *
   * @return the offset position
   */
  public Vector3f offset() {
    return this.offset;
  }

  /**
   * Sets the offset position of the text.
   *
   * @param offset the offset position to set
   * @return the updated Text3D object
   */
  public Text3D offset(Vector3f offset) {
    this.offset = offset;
    return this;
  }

  /**
   * Gets the billboard mode of the text.
   *
   * @return the billboard mode
   */
  public BillboardMode billboardMode() {
    return this.billboardMode;
  }

  /**
   * Sets the billboard mode of the text.
   *
   * @param billboardMode the billboard mode to set
   * @return the updated Text3D object
   */
  public Text3D billboardMode(BillboardMode billboardMode) {
    this.billboardMode = billboardMode;
    return this;
  }

  /**
   * Gets the font used for the text.
   *
   * @return the font
   */
  public Font font() {
    return this.font;
  }

  /**
   * Sets the font used for the text.
   *
   * @param font the font to set
   * @return the updated Text3D object
   */
  public Text3D font(Font font) {
    if (this.font == font) return this;
    this.font = font;
    this.updateMesh();
    return this;
  }

  /**
   * Gets the shader program used for rendering the text.
   *
   * @return the shader program
   */
  public ShaderProgram shader() {
    return this.shader;
  }

  /**
   * Sets the shader program used for rendering the text.
   *
   * @param shader the shader program to set
   * @return the updated Text3D object
   */
  public Text3D shader(ShaderProgram shader) {
    this.shader = shader;
    return this;
  }

  /**
   * Gets the text to display.
   *
   * @return the text
   */
  public String text() {
    return this.text;
  }

  /**
   * Sets the text to display.
   *
   * @param text the text to set
   * @return the updated Text3D object
   */
  public Text3D text(String text) {
    if (this.text.equals(text)) return this;
    this.text = text;
    this.updateMesh();
    return this;
  }

  /**
   * Gets the size of the text.
   *
   * @return the size
   */
  public Vector3f size() {
    return this.size;
  }

  /**
   * Sets the size of the text.
   *
   * @param size the size to set
   * @return the updated Text3D object
   */
  public Text3D size(Vector3f size) {
    if (this.size.equals(size)) return this;
    this.size = size;
    this.updateMesh();
    return this;
  }

  /**
   * Gets the alignment of the text.
   *
   * @return the alignment
   */
  public TextAlignment alignment() {
    return this.alignment;
  }

  /**
   * Sets the alignment of the text.
   *
   * @param alignment the alignment to set
   * @return the updated Text3D object
   */
  public Text3D alignment(TextAlignment alignment) {
    if (this.alignment == alignment) return this;
    this.alignment = alignment;
    this.updateMesh();
    return this;
  }

  /**
   * Gets the font size of the text.
   *
   * @return the font size
   */
  public int fontSize() {
    return this.fontSize;
  }

  /**
   * Sets the font size of the text.
   *
   * @param fontSize the font size to set
   * @return the updated Text3D object
   */
  public Text3D fontSize(int fontSize) {
    if (this.fontSize == fontSize) return this;
    this.fontSize = fontSize;
    this.updateMesh();
    return this;
  }

  /**
   * Gets the background color of the text.
   *
   * @return the background color
   */
  public int backgroundColor() {
    return this.backgroundColor;
  }

  /**
   * Sets the background color of the text.
   *
   * @param backgroundColor the background color to set
   * @return the updated Text3D object
   */
  public Text3D backgroundColor(int backgroundColor) {
    this.backgroundColor = backgroundColor;
    return this;
  }

  /**
   * Represents the origin mode of the text. The origin mode determines the reference point for
   * positioning the text.
   */
  public enum OriginMode {
    /** The center of the text is used as the origin. */
    CENTER,

    /** The top-left corner of the text is used as the origin. */
    TOP_LEFT
  }

  /**
   * Represents a render step for a specific texture page.
   *
   * @param pageGLHandle the OpenGL handle of the texture page
   * @param count the number of elements to render
   * @param offset the offset of the elements to render
   */
  private record RenderStep(int pageGLHandle, int count, int offset) {}
}

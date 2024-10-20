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
import java.io.IOException;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

/**
 * Represents a UI image element that can be rendered with OpenGL.
 * The UIImage class extends UIElement and provides methods to set and get
 * the image texture.
 */
public class UIImage extends UIElement<UIImage> {

  private static ShaderProgram SHADER;
  private static ArrayMesh MESH;

  /**
   * Initializes the OpenGL shader for the UIImage.
   * This method is called internally before rendering the image.
   */
  private static void initShader() {
    if (SHADER != null) return;
    try {
      Shader vertexShader = Shader.loadShader(Resource.load("/shaders/ui/Image.vsh"), Shader.ShaderType.VERTEX_SHADER);
      Shader fragmentShader = Shader.loadShader(Resource.load("/shaders/ui/Image.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
      SHADER = new ShaderProgram(vertexShader, fragmentShader);
    } catch(IOException ex) {
      throw new RuntimeException("Failed to load shader", ex);
    }
  }

  /**
   * Initializes the OpenGL mesh for the UIImage.
   * This method is called internally before rendering the image.
   */
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

  /**
   * Constructs a UIImage with the specified image resource.
   *
   * @param image the image resource to load as texture
   */
  public UIImage(Resource image) {
    this.texture = TextureManager.load(image);
  }

  /**
   * Sets the texture of the UIImage.
   *
   * @param resource the image resource to load as texture
   * @return this UIImage instance for method chaining
   */
  public UIImage texture(Resource resource) {
    this.texture = TextureManager.load(resource);
    return this;
  }

  /**
   * Renders the UIImage using the specified camera.
   * This method sets the shader uniforms and renders the mesh.
   *
   * @param camera the camera to use for rendering
   */
  @Override
  protected void render(Camera<?> camera) {
    initShader();
    initMesh();

    MESH.transformation(this.absolutePosition(), this.rotation, this.size);
    SHADER.bind();
    this.texture.bind(GL33.GL_TEXTURE0);
    SHADER.setUniform1i("uTexture", 0);
    MESH.render(camera, SHADER);
    SHADER.unbind();
    this.texture.unbind();
  }
}

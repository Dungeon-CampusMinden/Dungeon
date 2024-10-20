package de.fwatermann.dungine.utils;

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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

/**
 * The `CoordinateAxis` class represents a 3D coordinate axis renderable object.
 * It provides methods to initialize OpenGL resources, render the axis, and manage transformations.
 */
public class CoordinateAxis extends Renderable<CoordinateAxis> {

  private ArrayMesh mesh;
  private ShaderProgram shaderProgram;
  private boolean ignoreDepth;
  private BoundingBox boundingBox = new BoundingBox(0, 0, 0, 0, 0, 0);

  /**
   * Constructs a `CoordinateAxis` with the specified position, size, and depth ignore flag.
   *
   * @param position the position of the coordinate axis
   * @param size the size of the coordinate axis
   * @param ignoreDepth whether to ignore depth testing when rendering
   */
  public CoordinateAxis(Vector3f position, float size, boolean ignoreDepth) {
    super(position, new Vector3f(size), new Quaternionf());
    this.ignoreDepth = ignoreDepth;
    this.initGL();
  }

  /**
   * Initializes OpenGL resources for the coordinate axis.
   */
  private void initGL() {
    ByteBuffer verticesB = BufferUtils.createByteBuffer(6 * 6 * 4);
    FloatBuffer vertices = verticesB.asFloatBuffer();
    vertices.position(0);
    vertices.put(
        new float[] {
          0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
          1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
          0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f,
          0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
          0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,
          0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f
        });
    vertices.flip();
    verticesB.position(0);

    this.mesh =
        new ArrayMesh(
            verticesB,
            PrimitiveType.LINES,
            GLUsageHint.DRAW_STATIC,
            new VertexAttribute(3, DataType.FLOAT, "a_Position"),
            new VertexAttribute(3, DataType.FLOAT, "a_Color"));
    try {
      Shader vertexShader =
          Shader.loadShader(
              Resource.load("/shaders/CoordinateAxis.vsh"), Shader.ShaderType.VERTEX_SHADER);
      Shader fragmentShader =
          Shader.loadShader(
              Resource.load("/shaders/CoordinateAxis.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
      this.shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Renders the coordinate axis using the specified camera.
   *
   * @param camera the camera to use for rendering
   */
  @Override
  public void render(Camera<?> camera) {
    ThreadUtils.checkMainThread();
    this.render(camera, this.shaderProgram);
  }

  /**
   * Renders the coordinate axis using the specified camera and shader program.
   *
   * @param camera the camera to use for rendering
   * @param shader the shader program to use for rendering
   */
  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    boolean depthTestState = GL33.glIsEnabled(GL33.GL_DEPTH_TEST);
    if (this.ignoreDepth && depthTestState) {
      GL33.glDisable(GL33.GL_DEPTH_TEST);
    }
    shader.bind();
    GL33.glLineWidth(5.0f);
    this.mesh.transformation(this.position(), this.rotation(), this.scaling());
    this.mesh.render(camera, shader);
    shader.unbind();

    if (this.ignoreDepth && depthTestState) {
      GL33.glEnable(GL33.GL_DEPTH_TEST);
    }
  }

  /**
   * Returns whether depth testing is ignored when rendering.
   *
   * @return true if depth testing is ignored, false otherwise
   */
  public boolean ignoreDepth() {
    return this.ignoreDepth;
  }

  /**
   * Sets whether depth testing is ignored when rendering.
   *
   * @param ignoreDepth true to ignore depth testing, false otherwise
   * @return this `CoordinateAxis` instance
   */
  public CoordinateAxis ignoreDepth(boolean ignoreDepth) {
    this.ignoreDepth = ignoreDepth;
    return this;
  }

  /**
   * Called when the transformation of the coordinate axis changes.
   */
  @Override
  protected void transformationChanged() {
    super.transformationChanged();
    if(this.mesh != null)
      this.boundingBox =
        BoundingBox.fromVertices(
            this.mesh.vertexBuffer().asFloatBuffer(), 0, 6, 6, this.transformationMatrix());
  }

  /**
   * Returns the mesh of the coordinate axis.
   *
   * @return the mesh of the coordinate axis
   */
  public ArrayMesh mesh() {
    return this.mesh;
  }

  /**
   * Determines whether the coordinate axis should be rendered based on the camera frustum.
   *
   * @param frustum the camera frustum
   * @return true if the coordinate axis should be rendered, false otherwise
   */
  @Override
  public boolean shouldRender(CameraFrustum frustum) {
    int frustumResult = frustum.intersectAab(this.boundingBox.getMin(), this.boundingBox.getMax());
    return frustumResult == CameraFrustum.INTERSECT || frustumResult == CameraFrustum.INSIDE;
  }
}

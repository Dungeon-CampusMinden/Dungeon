package de.fwatermann.dungine.graphics;

import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraFrustum;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.utils.Disposable;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

/**
 * The `Grid3D` class represents a 3D grid that can be rendered in the game. It handles the
 * initialization of OpenGL resources, rendering of the grid, and disposal of resources.
 */
public class Grid3D extends Renderable<Grid3D> implements Disposable {

  private static ShaderProgram SHADER;

  private int vao;
  private int vbo;
  private boolean initialized = false;

  /** Constructs a new `Grid3D` object. */
  public Grid3D() {}

  private void initGL() {
    if (SHADER == null) {
      Shader vertexShader = new Shader(VERTEX_SHADER, Shader.ShaderType.VERTEX_SHADER);
      Shader fragmentShader = new Shader(FRAGMENT_SHADER, Shader.ShaderType.FRAGMENT_SHADER);
      SHADER = new ShaderProgram(vertexShader, fragmentShader);
    }
    if (this.initialized) return;

    ByteBuffer vertices = BufferUtils.createByteBuffer(6 * 3 * 4);
    vertices
        .asFloatBuffer()
        .put(
            new float[] {
              -1.0f, -1.0f, 0.0f,
              1.0f, -1.0f, 0.0f,
              1.0f, 1.0f, 0.0f,
              1.0f, 1.0f, 0.0f,
              -1.0f, 1.0f, 0.0f,
              -1.0f, -1.0f, 0.0f
            })
        .flip();

    this.vao = GL33.glGenVertexArrays();
    this.vbo = GL33.glGenBuffers();

    GL33.glBindVertexArray(this.vao);
    GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, this.vbo);
    GL33.glBufferData(GL33.GL_ARRAY_BUFFER, vertices, GL33.GL_STATIC_DRAW);

    int loc = SHADER.getAttributeLocation("aPosition");
    GL33.glEnableVertexAttribArray(loc);
    GL33.glVertexAttribPointer(loc, 3, GL33.GL_FLOAT, false, 3 * 4, 0);

    GL33.glBindVertexArray(0);
    GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);

    this.initialized = true;
  }

  /**
   * Renders the grid using the specified camera.
   *
   * @param camera the camera to use for rendering
   */
  @Override
  public void render(Camera<?> camera) {
    this.initGL();
    this.render(camera, SHADER);
  }

  /**
   * Renders the grid using the specified camera and shader program.
   *
   * @param camera the camera to use for rendering
   * @param shader the shader program to use for rendering
   */
  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    this.initGL();
    shader.bind();
    shader.useCamera(camera);
    shader.setUniform3f("uPosition", camera.position());
    GL33.glBindVertexArray(this.vao);
    GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, 6);
    GL33.glBindVertexArray(0);
    shader.unbind();
  }

  /**
   * Disposes of the OpenGL resources used by the grid. This includes deleting vertex array objects
   * and vertex buffer objects.
   */
  @Override
  public void dispose() {
    if (this.initialized) {
      GL33.glDeleteVertexArrays(this.vao);
      GL33.glDeleteBuffers(this.vbo);
      this.initialized = false;
    }
  }

  /**
   * Determines whether the grid should be rendered based on the camera frustum.
   *
   * @param frustum the camera frustum to check
   * @return true if the grid should be rendered, false otherwise
   */
  @Override
  public boolean shouldRender(CameraFrustum frustum) {
    return true;
  }

  private static final String VERTEX_SHADER =
      """
#version 330 core

layout (location = 0) in vec3 aPosition;

uniform mat4 uView;
uniform mat4 uProjection;
uniform vec3 uPosition;

out vec3 nearPoint;
out vec3 farPoint;
out mat4 fragView;
out mat4 fragProj;

vec3 unprojectPoint(float x, float y, float z, mat4 view, mat4 projection) {
  mat4 viewInv = inverse(view);
  mat4 projInv = inverse(projection);
  vec4 unprojectedPoint = viewInv * projInv * vec4(x, y, z, 1.0);
  return unprojectedPoint.xyz / unprojectedPoint.w;
}

void main() {
  vec3 p = aPosition;
  nearPoint = unprojectPoint(p.x, p.y, 0.0, uView, uProjection).xyz;
  farPoint = unprojectPoint(p.x, p.y, 1.0, uView, uProjection).xyz;
  fragView = uView;
  fragProj = uProjection;
  gl_Position = vec4(p, 1.0);
}
""";

  private static final String FRAGMENT_SHADER =
      """
#version 330 core

in vec3 nearPoint;
in vec3 farPoint;
in mat4 fragView;
in mat4 fragProj;

uniform float uNear;
uniform float uFar;

out vec4 outColor;

vec4 grid(vec3 fragPos3D, float scale, bool drawAxis) {
  vec2 coord = fragPos3D.xz * scale;
  vec2 derivative = fwidth(coord);
  vec2 grid = abs(fract(coord - 0.5) - 0.5) / derivative;
  float line = min(grid.x, grid.y);
  float minimumz = min(derivative.y, 1);
  float minimumx = min(derivative.x, 1);
  vec4 color = vec4(0.2, 0.2, 0.2, 1.0 - min(line, 1.0));
  // z axis
  if (fragPos3D.x > -0.1 * minimumx && fragPos3D.x < 0.1 * minimumx)
  color.z = 1.0;
  // x axis
  if (fragPos3D.z > -0.1 * minimumz && fragPos3D.z < 0.1 * minimumz)
  color.x = 1.0;
  return color;
}

float computeDepth(vec3 pos) {
  vec4 clip_space_pos = fragProj * fragView * vec4(pos.xyz, 1.0);
  return (clip_space_pos.z / clip_space_pos.w) * 0.5 + 0.5; // put back between 0 and 1
}

float computeLinearDepth(vec3 pos) {
  vec4 clip_space_pos = fragProj * fragView * vec4(pos.xyz, 1.0);
  float clip_space_depth = (clip_space_pos.z / clip_space_pos.w) * 2.0 - 1.0; // put back between -1 and 1
  float linearDepth = (2.0 * uNear * uFar) / (uFar + uNear - clip_space_depth * (uFar - uNear)); // get linear value between 0.01 and 100
  return linearDepth / uFar; // normalize
}

void main() {
  float t = -nearPoint.y / (farPoint.y - nearPoint.y);
  vec3 fragPos3D = nearPoint + t * (farPoint - nearPoint);

  float linearDepth = computeLinearDepth(fragPos3D);
  float fading = max(0, (0.5 - linearDepth));

  gl_FragDepth = computeDepth(fragPos3D);

  outColor = (grid(fragPos3D, 10, true) + grid(fragPos3D, 1, true)) * float(t > 0); // adding multiple resolution for the grid
  outColor.a *= fading;
}
""";
}

package de.fwatermann.dungine.graphics.simple;

import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.graphics.texture.Texture;
import de.fwatermann.dungine.graphics.texture.TextureManager;
import de.fwatermann.dungine.resource.Resource;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

public class PaneTextured extends Pane {

  private static ShaderProgram SHADER;

  private Resource resource;
  private Texture texture;

  public PaneTextured(Vector3f position, Vector3f size, Resource texture) {
    super(position, size);
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

  public void texture(Resource texture) {
    this.resource = texture;
    if (this.texture != null) this.texture.dispose();
  }

  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    if (shader == null) return;

    if (this.texture == null) {
      this.texture = TextureManager.instance().load(this.resource);
    }

    this.texture.bind(GL33.GL_TEXTURE0);
    shader.bind();
    shader.setUniform1i("uTexture", 0);
    this.mesh.transformation(this.position(), this.rotation(), this.scaling());
    this.mesh.render(camera, shader);
    shader.unbind();
  }

  private static final String VERTEX_SHADER =
      """
#version 330 core

in vec3 a_Position;
in vec2 a_TexCoord;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

out vec2 vs_TexCoord;

void main() {
  gl_Position = uProjection * uView * uModel * vec4(a_Position, 1.0);
  vs_TexCoord = a_TexCoord;
}

""";

  private static final String FRAGMENT_SHADER =
      """
#version 330 core

in vec2 vs_TexCoord;

uniform sampler2D uTexture;

out vec4 fragColor;

void main() {
 fragColor = texture(uTexture, vs_TexCoord);
}
""";
}

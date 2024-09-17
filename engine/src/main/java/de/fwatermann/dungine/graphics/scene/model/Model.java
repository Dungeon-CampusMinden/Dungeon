package de.fwatermann.dungine.graphics.scene.model;


import de.fwatermann.dungine.graphics.Renderable;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraFrustum;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.resource.Resource;
import java.io.IOException;
import java.util.List;
import org.lwjgl.opengl.GL33;

public class Model extends Renderable<Model> {

  private static ShaderProgram DEFAULT_SHADER;

  private final Resource resource;
  private final List<Material> materials;
  private ShaderProgram shader;

  protected Model(Resource resource, List<Material> materials) {
    this.resource = resource;
    this.materials = materials;
  }

  private static void initShader() {
    if(DEFAULT_SHADER != null) return;
    try {
      Shader vertexShader = Shader.loadShader(Resource.load("/shaders/default/model.vsh"), Shader.ShaderType.VERTEX_SHADER);
      Shader geometryShader = Shader.loadShader(Resource.load("/shaders/default/model.gsh"), Shader.ShaderType.GEOMETRY_SHADER);
      Shader fragmentShader = Shader.loadShader(Resource.load("/shaders/default/model.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
      DEFAULT_SHADER = new ShaderProgram(vertexShader, geometryShader, fragmentShader);
    } catch(IOException ex) {
      throw new RuntimeException("Failed to load default shader!", ex);
    }
  }

  @Override
  public void render(Camera<?> camera) {
    if(this.shader == null) {
      initShader();
      this.render(camera, DEFAULT_SHADER);
    } else {
      this.render(camera, this.shader);
    }
  }

  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    shader.bind();
    shader.useCamera(camera);

    for(Material material : this.materials) {
      shader.setUniform4f("uMaterial.diffuseColor", material.diffuseColor);
      shader.setUniform1i("uMaterial.diffuseTexture", 0);
      shader.setUniform1i("uMaterial.ambientTexture", 1);
      shader.setUniform1i("uMaterial.specularTexture", 2);
      shader.setUniform1i("uMaterial.normalTexture", 3);
      shader.setUniform1i("uMaterial.flags", material.flags);

      if(material.transparent) {
        GL33.glBlendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA);
      } else {
        GL33.glBlendFunc(GL33.GL_ONE, GL33.GL_ZERO);
      }

      if(material.diffuseTexture != null) {
        material.diffuseTexture.bind(GL33.GL_TEXTURE0);
      }
      if(material.ambientTexture != null) {
        material.ambientTexture.bind(GL33.GL_TEXTURE1);
      }
      if(material.specularTexture != null) {
        material.specularTexture.bind(GL33.GL_TEXTURE2);
      }
      if(material.normalTexture != null) {
        material.normalTexture.bind(GL33.GL_TEXTURE3);
      }

      material.meshes.forEach(mesh -> {
        mesh.transformation(this.position(), this.rotation(), this.scaling());
        mesh.render(camera, shader);
      });
    }

    shader.unbind();
  }

  @Override
  public boolean shouldRender(CameraFrustum frustum) {
    return true;
    //TODO: Implement frustum culling for models.
  }
}

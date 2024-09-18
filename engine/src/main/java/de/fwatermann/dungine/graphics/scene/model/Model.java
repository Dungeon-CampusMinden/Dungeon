package de.fwatermann.dungine.graphics.scene.model;


import de.fwatermann.dungine.graphics.Renderable;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraFrustum;
import de.fwatermann.dungine.graphics.scene.SceneRenderer;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.graphics.texture.animation.Animation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.lwjgl.opengl.GL33;

public class Model extends Renderable<Model> {

  protected final List<Material> materials = new ArrayList<>();
  protected ShaderProgram shader;

  protected Model() {}

  protected Model(List<Material> materials) {
    this.materials.addAll(materials);
  }

  @Override
  public void render(Camera<?> camera) {
    if(this.shader == null) {
      this.render(camera, SceneRenderer.defaultShader());
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
      shader.setUniform4f("uMaterial.ambientColor", material.ambientColor);
      shader.setUniform4f("uMaterial.specularColor", material.specularColor);
      shader.setUniform1i("uMaterial.diffuseTexture", 0);
      shader.setUniform1i("uMaterial.ambientTexture", 1);
      shader.setUniform1i("uMaterial.specularTexture", 2);
      shader.setUniform1i("uMaterial.normalTexture", 3);
      shader.setUniform1i("uMaterial.flags", material.flags);

      GL33.glBlendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA);

      if(material.diffuseTexture != null) {
        material.diffuseTexture.bind(shader, Animation.AnimationSlot.ANIMATION_0, GL33.GL_TEXTURE0);
      }
      if(material.ambientTexture != null) {
        material.ambientTexture.bind(shader, Animation.AnimationSlot.ANIMATION_1, GL33.GL_TEXTURE2);
      }
      if(material.specularTexture != null) {
        material.specularTexture.bind(shader, Animation.AnimationSlot.ANIMATION_2, GL33.GL_TEXTURE4);
      }
      if(material.normalTexture != null) {
        material.normalTexture.bind(shader, Animation.AnimationSlot.ANIMATION_3, GL33.GL_TEXTURE6);
      }

      material.meshes.forEach(meshEntry -> {
        meshEntry.mesh().transformation(this.position(), this.rotation(), this.scaling());
        if(meshEntry.offset() == 0 && meshEntry.count() <= 0) {
          meshEntry.mesh().render(camera, shader);
        } else {
          meshEntry.mesh().render(camera, shader, meshEntry.offset(), meshEntry.count());
        }
      });
    }

    shader.unbind();
  }

  @Override
  public boolean shouldRender(CameraFrustum frustum) {
    return true;
    //TODO: Implement frustum culling for models.
  }

  public Stream<Material> materials() {
    return this.materials.stream();
  }

  public ShaderProgram shader() {
    return this.shader;
  }

  public Model shader(ShaderProgram shader) {
    this.shader = shader;
    return this;
  }
}

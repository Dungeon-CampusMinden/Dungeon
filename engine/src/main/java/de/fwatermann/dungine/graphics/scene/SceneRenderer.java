package de.fwatermann.dungine.graphics.scene;


import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.scene.light.Light;
import de.fwatermann.dungine.graphics.scene.model.Model;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.resource.Resource;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Set;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryStack;

public class SceneRenderer {

  private static ShaderProgram DEFAULT_SHADER;

  private static int glLightsUBO = -1;

  public static ShaderProgram defaultShader() {
    if(DEFAULT_SHADER == null) {
      try {
        Shader vertexShader = Shader.loadShader(Resource.load("/shaders/default/scene.vsh"), Shader.ShaderType.VERTEX_SHADER);
        Shader geometryShader = Shader.loadShader(Resource.load("/shaders/default/scene.gsh"), Shader.ShaderType.GEOMETRY_SHADER);
        Shader fragmentShader = Shader.loadShader(Resource.load("/shaders/default/scene.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
        DEFAULT_SHADER = new ShaderProgram(vertexShader, geometryShader, fragmentShader);
      } catch(IOException ex) {
        throw new RuntimeException("Failed to load default scene shader!", ex);
      }
    }
    return DEFAULT_SHADER;
  }

  public static void renderScene(Camera<?> camera, Set<Model> models, Set<Light<?>> lights) {
    renderScene(camera, models, lights, defaultShader());
  }

  public static void renderScene(Camera<?> camera, Set<Model> models, Set<Light<?>> lights, ShaderProgram shader) {
    if(glLightsUBO == -1) {
      glLightsUBO = GL33.glGenBuffers();
      GL33.glBindBuffer(GL33.GL_UNIFORM_BUFFER, glLightsUBO);
      GL33.glBufferData(GL33.GL_UNIFORM_BUFFER, Light.MAX_NUMBER_LIGHTS * Light.STRUCT_SIZE + 16, GL33.GL_DYNAMIC_DRAW);
      GL33.glBindBufferRange(GL33.GL_UNIFORM_BUFFER, 0, glLightsUBO, 0, Light.MAX_NUMBER_LIGHTS * Light.STRUCT_SIZE + 16);
      GL33.glBindBuffer(GL33.GL_UNIFORM_BUFFER, 0);
    }

    try(MemoryStack stack = MemoryStack.stackPush()) {
      ByteBuffer lightsBuffer = stack.calloc(16 + lights.size() * Light.STRUCT_SIZE);
      lightsBuffer.putInt(lights.size()).put(new byte[12]);
      for(Light<?> light : lights) {
        ByteBuffer struct = light.getStruct();
        lightsBuffer.put(struct);
      }
      lightsBuffer.flip();
      GL33.glBindBuffer(GL33.GL_UNIFORM_BUFFER, glLightsUBO);
      GL33.glBufferSubData(GL33.GL_UNIFORM_BUFFER, 0, lightsBuffer);
      GL33.glBindBuffer(GL33.GL_UNIFORM_BUFFER, 0);
    }

    shader.bind();
    shader.setUniformBlockBinding("uLighting", 0);

    models.stream().sorted((a, b) -> {
      float diff = a.position().distanceSquared(camera.position()) - b.position().distanceSquared(camera.position());
      return diff < 0 ? -1 : diff > 0 ? 1 : 0;
    }).forEach(m -> {
      m.render(camera, shader);
    });
    shader.unbind();
  }


}

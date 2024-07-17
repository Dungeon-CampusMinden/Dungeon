package de.fwatermann.dungine.graphics;

import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;

public interface IRenderable {

  void render(Camera<?> camera);

  void render(Camera<?> camera, ShaderProgram shader);

}

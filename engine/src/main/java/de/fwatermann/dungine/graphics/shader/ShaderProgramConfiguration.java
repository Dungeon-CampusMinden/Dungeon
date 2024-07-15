package de.fwatermann.dungine.graphics.shader;

/**
 * The ShaderProgramConfiguration class represents a configuration for a ShaderProgram object.
 */
public class ShaderProgramConfiguration {

  /** Uniform name of the mat4 that contains the View matrix. */
  public String uniformViewMatrix = "uView";

  /** Uniform name of the mat4 that contains the Projection matrix. */
  public String uniformProjectionMatrix = "uProjection";

  /** Uniform name of the mat4 that contains the Model/Transform matrix. */
  public String uniformModelMatrix = "uModel";

  public String uniformBlockTextureAtlas = "ubTextureAtlas";


  public ShaderProgramConfiguration() {}

}

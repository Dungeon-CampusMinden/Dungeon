package de.fwatermann.dungine.graphics.shader;

/**
 * The ShaderProgramConfiguration class represents a configuration for a ShaderProgram object.
 */
public class ShaderProgramConfiguration {

  private String uniformViewMatrix = "uView";
  private String uniformProjectionMatrix = "uProjection";
  private String uniformModelMatrix = "uModel";

  public ShaderProgramConfiguration() {}

  /**
   * Get the uniform name for the view matrix.
   * Default: "uView"
   * @return the uniform name for the view matrix
   */
  public String uniformViewMatrix() {
    return this.uniformViewMatrix;
  }

  /**
   * Set the uniform name for the view matrix.
   * @param uniformViewMatrix the uniform name for the view matrix
   * @return this ShaderProgramConfiguration object
   */
  public ShaderProgramConfiguration uniformViewMatrix(String uniformViewMatrix) {
    this.uniformViewMatrix = uniformViewMatrix;
    return this;
  }

  /**
   * Get the uniform name for the projection matrix.
   * Default: "uProjection"
   * @return the uniform name for the projection matrix
   */
  public String uniformProjectionMatrix() {
    return this.uniformProjectionMatrix;
  }

  /**
   * Set the uniform name for the projection matrix.
   * @param uniformProjectionMatrix the uniform name for the projection matrix
   * @return this ShaderProgramConfiguration object
   */
  public ShaderProgramConfiguration uniformProjectionMatrix(String uniformProjectionMatrix) {
    this.uniformProjectionMatrix = uniformProjectionMatrix;
    return this;
  }

  /**
   * Get the uniform name for the model matrix.
   * Default: "uModel"
   * @return the uniform name for the model matrix
   */
  public String uniformModelMatrix() {
    return this.uniformModelMatrix;
  }

  /**
   * Set the uniform name for the model matrix.
   * @param uniformModelMatrix the uniform name for the model matrix
   * @return this ShaderProgramConfiguration object
   */
  public ShaderProgramConfiguration uniformModelMatrix(String uniformModelMatrix) {
    this.uniformModelMatrix = uniformModelMatrix;
    return this;
  }
}

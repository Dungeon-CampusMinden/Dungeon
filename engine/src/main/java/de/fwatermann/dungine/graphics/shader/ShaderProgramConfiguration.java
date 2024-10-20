package de.fwatermann.dungine.graphics.shader;

/** The ShaderProgramConfiguration class represents a configuration for a ShaderProgram object. */
public class ShaderProgramConfiguration {

  /**
   * The ShaderProgramConfigurationMaterial class represents a configuration for the material
   */
  public final ShaderProgramConfigurationMaterial material = new ShaderProgramConfigurationMaterial();

  /** Uniform name of the mat4 that contains the View matrix. */
  public String uniformViewMatrix = "uView";

  /** Uniform name of the mat4 that contains the Projection matrix. */
  public String uniformProjectionMatrix = "uProjection";

  /** Uniform name of the float that contains the near plane distance of a perspective camera. */
  public String uniformCameraPerspectiveNearPlane = "uNear";

  /** Uniform name of the float that contains the far plane distance of a perspective camera. */
  public String uniformCameraPerspectiveFarPlane = "uFar";

  /** Uniform name of the vec3 that contains the camera position. */
  public String uniformCameraPosition = "uCameraPosition";

  /** Uniform name of the mat4 that contains the Model/Transform matrix. */
  public String uniformModelMatrix = "uModel";

  /** Uniform name of the vec2 that contains the size of the texture atlas. */
  public String uniformTextureAtlasSize = "uTextureAtlasSize";

  /** Uniform name that contains the texture atlas entry information. */
  public String uniformTextureAtlasEntrySampler = "uTextureAtlasEntries";

  /** Uniform name of the sampler2D array that contains the texture atlas pages. */
  public String uniformTextureAtlasPagesSamplerArray = "uTextureAtlasPages";


  /**
   * Name of the uniform array that contains the animation information.
   *
   * <p>Must be an array! Example: <code>"uAnimation[%d]"</code> where <code>%d</code> will be
   * replaced with the animation slot.
   */
  public String uniformAnimation = "uAnimation[%d]";

  /** Create a new ShaderProgramConfiguration instance. */
  public ShaderProgramConfiguration() {}

  /**
   * The ShaderProgramConfigurationMaterial class represents a configuration for the material
   * information in a ShaderProgram object.
   */
  public static final class ShaderProgramConfigurationMaterial {

    private ShaderProgramConfigurationMaterial() {}

    /**
     * Uniform name of the struct that contains the material information.
     */
    public String struct = "uMaterial";

    /**
     * Uniform name of the vec4 that contains the diffuse color.
     */
    public String diffuseColor = "diffuseColor";

    /**
     * Full name of the diffuse color uniform.
     * @return combined uniform name
     */
    public String diffuseColor() {
      return this.struct + "." + this.diffuseColor;
    }

    /**
     * Uniform name of the vec4 that contains the ambient color.
     */
    public String ambientColor = "ambientColor";

    /**
     * Full name of the ambient color uniform.
     * @return combined uniform name
     */
    public String ambientColor() {
      return this.struct + "." + this.ambientColor;
    }

    /**
     * Uniform name of the vec4 that contains the specular color.
     */
    public String specularColor = "specularColor";

    /**
     * Full name of the specular color uniform.
     * @return combined uniform name
     */
    public String specularColor() {
      return this.struct + "." + this.specularColor;
    }

    /**
     * Uniform name of the int that contains the flags.
     */
    public String diffuseTexture = "diffuseTexture";

    /**
     * Full name of the diffuse texture uniform.
     * @return combined uniform name
     */
    public String diffuseTexture() {
      return this.struct + "." + this.diffuseTexture;
    }

    /**
     * Uniform name of the int that contains the flags.
     */
    public String ambientTexture = "ambientTexture";

    /**
     * Full name of the ambient texture uniform.
     * @return combined uniform name
     */
    public String ambientTexture() {
      return this.struct + "." + this.ambientTexture;
    }

    /**
     * Uniform name of the int that contains the flags.
     */
    public String specularTexture = "specularTexture";

    /**
     * Full name of the specular texture uniform.
     * @return combined uniform name
     */
    public String specularTexture() {
      return this.struct + "." + this.specularTexture;
    }

    /**
     * Uniform name of the int that contains the flags.
     */
    public String normalTexture = "normalTexture";

    /**
     * Full name of the normal texture uniform.
     * @return combined uniform name
     */
    public String normalTexture() {
      return this.struct + "." + this.normalTexture;
    }

    /**
     * Uniform name of the int that contains the flags.
     */
    public String flags = "flags";

    /**
     * Full name of the flags uniform.
     * @return combined uniform name
     */
    public String flags() {
      return this.struct + "." + this.flags;
    }

  }

}

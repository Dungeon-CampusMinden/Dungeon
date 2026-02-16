package util.shaders;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import core.utils.Point;
import core.utils.Rectangle;
import core.utils.components.draw.shader.AbstractShader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A shader that applies lighting effects based on multiple light sources and ambient light.
 */
public class LightingShader extends AbstractShader {

  private static final String VERT_PATH = "shaders/passthrough.vert";
  private static final String FRAG_PATH = "shaders/lighting.frag";

  private float ambientLight = 0.2f;
  private final Set<Light> lightSources = new HashSet<>();

  /**
   * Constructs a Lighting shader.
   */
  public LightingShader() {
    super(VERT_PATH, FRAG_PATH);
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    List<Vector3> lightData = lightSources.stream()
        .map(light -> new Vector3(light.position().x(), light.position().y(), light.intensity()))
        .toList();
    List<Vector3> lightColors = lightSources.stream()
        .map(light -> {
          Color c = light.color();
          return new Vector3(c.r, c.g, c.b);
        })
        .toList();
    return List.of(
      new FloatUniform("u_ambientLight", ambientLight),
      new Vector3ArrayUniform("u_lightSources", lightData),
      new Vector3ArrayUniform("u_lightColors", lightColors)
    );
  }

  @Override
  public int padding() {
    return 0;
  }

  @Override
  public Rectangle worldBounds() {
    return null;
  }

  /**
   * Sets the ambient light level for the shader.
   * @param ambientLight the ambient light level
   * @return the shader instance for chaining
   */
  public LightingShader ambientLight(float ambientLight) {
    this.ambientLight = ambientLight;
    return this;
  }
  public float ambientLight() {
    return ambientLight;
  }

  /**
   * Adds a light source to the shader with the specified position and intensity.
   * @param position the position of the light source
   * @param intensity the intensity of the light source
   * @param color the color of the light source
   */
  public void addLightSource(Point position, float intensity, Color color) {
    lightSources.add(new Light(position, intensity, color));
  }

  /**
   * Adds a light source to the shader with the specified position and intensity.
   * @param position the position of the light source
   * @param intensity the intensity of the light source
   */
  public void addLightSource(Point position, float intensity) {
    addLightSource(position, intensity, Color.WHITE);
  }

  /**
   * Removes all light sources from the shader.
   */
  public void clearLightSources() {
    lightSources.clear();
  }

  /**
   * A record representing a light source with a position and intensity.
   * @param position the position of the light source
   * @param intensity the intensity of the light source
   */
  public record Light(Point position, float intensity, Color color) {}
}

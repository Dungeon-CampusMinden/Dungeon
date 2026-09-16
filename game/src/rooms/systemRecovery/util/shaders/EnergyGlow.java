package rooms.systemRecovery.util.shaders;

import com.badlogic.gdx.graphics.Color;
import engine.components.DrawComponent;
import engine.utils.components.draw.shader.ShineShader;

/** Shared visual effect for containers that visibly contain energy. */
public final class EnergyGlow {

  /** Identifier used by the runtime draw shader list. */
  public static final String SHADER_ID = "energyGlow";

  private static final float GLOW_HUE = 0.7f;

  private EnergyGlow() {}

  /**
   * Creates the small cyan sweep used around filled energy containers.
   *
   * @return a configured glow shader
   */
  public static ShineShader create() {
    Color glowColor = new Color().fromHsv(GLOW_HUE * 360f, 0.85f, 1f);
    glowColor.a = 0.85f;
    return new ShineShader()
        .padding(2)
        .sliceCount(2)
        .gapSize(0.8f)
        .rotationSpeed(0.35f)
        .shineColor(glowColor);
  }

  /**
   * Adds the glow to a runtime draw component if it is not already present.
   *
   * @param draw draw component receiving the glow
   */
  public static void addTo(DrawComponent draw) {
    if (draw.shaders().get(SHADER_ID) == null) {
      draw.shaders().add(SHADER_ID, create(), 1);
    }
  }
}

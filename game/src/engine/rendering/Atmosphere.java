package engine.rendering;

import com.badlogic.gdx.graphics.Color;

/**
 * The lighting mood of a level, applied by the atmosphere renderer of {@link
 * engine.systems.DrawSystem}.
 *
 * <p>A level returns its atmosphere from {@link engine.level.elements.ILevel#atmosphere()}; the
 * renderer reads it every frame, so a level may switch it at any time (lights off, alarm). Point
 * lights (torches, lamps, the players' lanterns and the level's own {@link
 * engine.level.elements.ILevel#lights lights}) add to the ambient light; where the ambient light is
 * brighter than half, they are toned down, so a fully lit room glows around its lamps instead of
 * washing out. Start from a preset and adjust it with the {@code with} methods.
 *
 * @param ambient the light every surface receives without a point light nearby; white shows the
 *     textures in their own colours, darker or tinted values set a cold or warm mood
 * @param shadowTint colour grade of dark areas, white for none
 * @param lightTint colour grade of bright areas, white for none
 * @param fog strength of the drifting fog, 0 for none
 * @param vignette strength of the darkened screen edges, 0 for none
 * @param lantern intensity of the light every player carries, 0 for none
 * @param wallDecor whether walls of the default dungeon design get torches, banners and fountains
 */
public record Atmosphere(
    Color ambient,
    Color shadowTint,
    Color lightTint,
    float fog,
    float vignette,
    float lantern,
    boolean wallDecor) {

  /** Fully lit, textures in their own colours; the default of every level. */
  public static final Atmosphere NEUTRAL =
      new Atmosphere(Color.WHITE, Color.WHITE, Color.WHITE, 0f, 0.35f, 0f, false);

  /** Bright and slightly warm, for friendly rooms such as classrooms or a kindergarten. */
  public static final Atmosphere DAYLIGHT =
      new Atmosphere(
          new Color(1f, 0.98f, 0.93f, 1f),
          new Color(0.98f, 0.99f, 1.03f, 1f),
          new Color(1.03f, 1f, 0.95f, 1f),
          0f,
          0.3f,
          0f,
          false);

  /** Torch-lit darkness with fog: cool shadows, warm light and dungeon wall decoration. */
  public static final Atmosphere DUNGEON =
      new Atmosphere(
          new Color(0.22f, 0.23f, 0.36f, 1f),
          new Color(0.88f, 0.96f, 1.16f, 1f),
          new Color(1.04f, 1f, 0.93f, 1f),
          1f,
          1f,
          0.85f,
          true);

  /** Dim and cool, for labs, server rooms or ice caves. */
  public static final Atmosphere COLD =
      new Atmosphere(
          new Color(0.48f, 0.55f, 0.68f, 1f),
          new Color(0.9f, 0.97f, 1.12f, 1f),
          new Color(0.96f, 1f, 1.06f, 1f),
          0.4f,
          0.6f,
          0.7f,
          false);

  /**
   * Returns this atmosphere with another ambient light.
   *
   * @param ambient the new ambient light
   * @return the changed atmosphere
   */
  public Atmosphere withAmbient(Color ambient) {
    return new Atmosphere(ambient, shadowTint, lightTint, fog, vignette, lantern, wallDecor);
  }

  /**
   * Returns this atmosphere with its ambient light scaled, keeping its colour.
   *
   * @param brightness factor for the ambient light, 0 for darkness
   * @return the changed atmosphere
   */
  public Atmosphere withBrightness(float brightness) {
    return withAmbient(
        new Color(ambient.r * brightness, ambient.g * brightness, ambient.b * brightness, 1f));
  }

  /**
   * Returns this atmosphere with another lantern intensity.
   *
   * @param lantern intensity of the light every player carries, 0 for none
   * @return the changed atmosphere
   */
  public Atmosphere withLantern(float lantern) {
    return new Atmosphere(ambient, shadowTint, lightTint, fog, vignette, lantern, wallDecor);
  }
}

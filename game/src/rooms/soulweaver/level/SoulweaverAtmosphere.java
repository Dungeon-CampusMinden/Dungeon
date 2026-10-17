package rooms.soulweaver.level;

import com.badlogic.gdx.graphics.Color;
import engine.Game;
import engine.components.DrawComponent;
import engine.rendering.Atmosphere;
import engine.rendering.Light;
import engine.systems.DrawSystem;
import engine.utils.Rectangle;
import engine.utils.components.draw.shader.ColorGradeShader;
import java.util.List;
import rooms.soulweaver.state.VariablePuzzleStage;

/**
 * Lighting, colour grading and steam of the room. Torches and kettles light up through the shared
 * renderer; when every torch is out, the room falls into near darkness. Once awakened, Nox glows
 * violet from the heart in its chest.
 */
final class SoulweaverAtmosphere {
  // A dim, cool stone hall; the light grade cools the torchlight a little.
  private static final Atmosphere LIT =
      new Atmosphere(
          new Color(0.38f, 0.42f, 0.52f, 1f),
          Color.WHITE,
          new Color(0.95f, 1f, 1.05f, 1f),
          0f,
          0.5f,
          0f,
          false);
  private static final Atmosphere BLACKOUT = LIT.withBrightness(0.06f);
  private static final String GOLEM = "soulweaver-variables-golem";
  private static final Color HEART_LIGHT = new Color(0.62f, 0.28f, 1f, 1f);

  private SoulweaverAtmosphere() {}

  /**
   * The room's atmosphere: lit while any torch burns.
   *
   * @return the current atmosphere
   */
  static Atmosphere current() {
    return SoulweaverProps.blackout() ? BLACKOUT : LIT;
  }

  /**
   * Adds the violet glow of Nox's heart once the binding has awakened it.
   *
   * @param out receives the lights
   */
  static void lights(List<Light> out) {
    boolean awake =
        SoulweaverBinding.state()
            .map(state -> state.stage() == VariablePuzzleStage.COMPLETE)
            .orElse(false);
    if (!awake) return;
    Game.levelEntities()
        .filter(entity -> entity.name().equals(GOLEM))
        .forEach(
            golem -> {
              DrawComponent draw = golem.fetch(DrawComponent.class).orElse(null);
              if (draw == null || !draw.isVisible() || (draw.tintColor() & 0xFF) == 0) return;
              Game.positionOf(golem)
                  // The heart sits in the middle of the chest.
                  .map(at -> at.translate(2.5f, 3.3f))
                  .ifPresent(at -> out.add(new Light(at, HEART_LIGHT, 6f, 0.9f, 0.08f)));
            });
  }

  static void install() {
    if (Game.systems().get(DrawSystem.class) instanceof DrawSystem draw) {
      Game.currentLevel()
          .ifPresent(
              level ->
                  draw.sceneShaders()
                      .add(
                          "soulweaver-forge",
                          new ColorGradeShader(-1f, 0.94f, 1.04f)
                              .region(
                                  new Rectangle(
                                      level.layout()[0].length, level.layout().length, 0, 0))));
      draw.sceneShaders().add("soulweaver-steam", new SoulweaverSteamShader());
      Game.currentLevel()
          .ifPresent(
              level -> {
                // The exit mask starts at the top of the north wall, so the wall shows whole.
                for (String area : new String[] {"east", "north", "exit"}) {
                  draw.sceneShaders()
                      .add(
                          "soulweaver-reveal-" + area,
                          new SoulweaverPassageRevealShader(
                              level.namedPoints().get("reveal-" + area + "-start"),
                              level.namedPoints().get("reveal-" + area + "-end"),
                              level
                                  .namedPoints()
                                  .get(
                                      area.equals("exit")
                                          ? "act2-gate-start"
                                          : "act1-gate-start")));
                }
              });
    }
  }
}

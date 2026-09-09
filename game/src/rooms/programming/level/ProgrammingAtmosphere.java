package rooms.programming.level;

import engine.Game;
import engine.systems.DrawSystem;
import engine.utils.Rectangle;
import engine.utils.components.draw.shader.ColorGradeShader;

/** Color grading and local room lights, using the shared renderer. */
final class ProgrammingAtmosphere {
  private ProgrammingAtmosphere() {}

  static void install() {
    if (Game.systems().get(DrawSystem.class) instanceof DrawSystem draw) {
      Game.currentLevel()
          .ifPresent(
              level ->
                  draw.sceneShaders()
                      .add(
                          "programming-forge",
                          new ColorGradeShader(-1f, 0.94f, 1.04f)
                              .region(
                                  new Rectangle(
                                      level.layout()[0].length, level.layout().length, 0, 0))));
      draw.sceneShaders().add("programming-lights", new ProgrammingLightingShader());
      Game.currentLevel()
          .ifPresent(
              level -> {
                for (String area : new String[] {"east", "north"}) {
                  draw.sceneShaders()
                      .add(
                          "programming-reveal-" + area,
                          new ProgrammingPassageRevealShader(
                              level.namedPoints().get("reveal-" + area + "-start"),
                              level.namedPoints().get("reveal-" + area + "-end"),
                              level.namedPoints().get("act1-gate-start")));
                }
              });
    }
  }
}

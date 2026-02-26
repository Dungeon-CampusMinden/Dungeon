package level;

import com.badlogic.gdx.graphics.Color;
import contrib.entities.HeroController;
import contrib.modules.keypad.KeypadComponent;
import contrib.utils.EntityUtils;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.level.DungeonLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.systems.DrawSystem;
import core.utils.Point;
import core.utils.components.draw.shader.OutlineShader;
import java.util.Map;
import java.util.Optional;
import modules.computer.*;
import util.shaders.LightingShader;

/** The Last Hour Room. */
public class LastHourLevelClient extends DungeonLevel {

  private static Entity keypad;
  private static Entity pc;

  /** The state of the PC when it's off. */
  public static final String PC_STATE_OFF = "off";

  private static final String PC_STATE_ON = "on";
  private static final String PC_STATE_VIRUS = "virus";
  private static final String PC_SIGNAL_ON = "on";
  private static final String PC_SIGNAL_INFECT = "infect";
  private static final String PC_SIGNAL_CLEAR = "clear";

  /**
   * Creates a new Demo Level.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param namedPoints The custom points of the level.
   */
  public LastHourLevelClient(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "last-hour-1");
  }

  @Override
  protected void onFirstTick() {
    setupLightingShader();
  }

  private void setupLightingShader() {
    DrawSystem.getInstance().sceneShaders().add("lighting", new LightingShader().ambientLight(0));
  }

  @Override
  protected void onTick() {
    checkInteractFeedback();
    updateLightingShader();

    findEntities();
  }

  private void findEntities() {
    if (pc == null) {
      Game.levelEntities()
          .filter(e -> e.name().equals("pc-main"))
          .findFirst()
          .ifPresent(e -> pc = e);
    }
    if (keypad == null) {
      Game.levelEntities()
          .filter(e -> e.name().equals("keypad-main"))
          .findFirst()
          .ifPresent(e -> keypad = e);
    }
  }

  private void updateLightingShader() {
    if (!(DrawSystem.getInstance().sceneShaders().get("lighting") instanceof LightingShader ls))
      return;

    ls.clearLightSources();

    if (ComputerStateComponent.getState().state().hasReached(ComputerProgress.ON)) {
      ls.ambientLight(0.2f);
      Color color = ComputerStateComponent.getState().isInfected() ? Color.RED : Color.BLUE;
      float intensity = ComputerStateComponent.getState().isInfected() ? 0.8f : 0.5f;
      ls.addLightSource(EntityUtils.getPosition(pc), intensity, color);

      ls.addLightSource(getPoint("timer").translate(0.75f, 0), 0.5f, Color.RED);

      var keyComp = keypad.fetch(KeypadComponent.class).orElseThrow();
      Color keypadColor = keyComp.isUnlocked() ? Color.GREEN : Color.RED;
      ls.addLightSource(getPoint("keypad-storage").translate(0.5f, 0.5f), 0.3f, keypadColor);
    }

    Game.allPlayers().forEach(e -> ls.addLightSource(EntityUtils.getPosition(e), 1f));
  }

  private Entity interactableEntity = null;

  private void checkInteractFeedback() {
    Game.player()
        .ifPresent(
            p -> {
              Optional<Entity> found =
                  HeroController.findInteractable(p, SkillTools.cursorPositionAsPoint());
              if (found.isPresent() && found.get() != interactableEntity) {
                // New interactable entity
                if (interactableEntity != null) {
                  // Remove old feedback
                  removeInteractFeedback(interactableEntity);
                }
                interactableEntity = found.get();
                addInteractFeedback(interactableEntity);
              } else if (found.isEmpty() && interactableEntity != null) {
                // No interactable entity anymore, remove old feedback
                removeInteractFeedback(interactableEntity);
                interactableEntity = null;
              }
            });
  }

  private void removeInteractFeedback(Entity entity) {
    entity
        .fetch(DrawComponent.class)
        .ifPresent(
            dc -> {
              dc.shaders().remove("outline");
            });
  }

  private void addInteractFeedback(Entity entity) {
    entity
        .fetch(DrawComponent.class)
        .ifPresent(
            dc -> {
              dc.shaders().add("outline", new OutlineShader(1, new Color(0.8f, 0, 0, 1f)));
            });
  }
}

package contrib.systems;

import static contrib.hud.UIUtils.defaultSkin;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import contrib.components.HealthComponent;
import contrib.components.UIComponent;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.systems.CameraSystem;
import core.utils.Point;
import core.utils.logging.CustomLogLevel;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Creates a progress bar which follows the associated Entity and shows the current health
 * percentage. *
 *
 * <p>Entities with the {@link HealthComponent} and {@link PositionComponent} will be processed by
 * this system.
 */
public final class HealthBarSystem extends System {

  // well percentage =)
  private static final float MIN = 0;
  private static final float MAX = 1;
  // bar percentage precision
  private static final float STEP_SIZE = 0.01f;
  private static final Logger LOGGER = Logger.getLogger(HealthBarSystem.class.getSimpleName());
  // how long the change to the health bar should take in seconds
  private static final float HEALTH_BAR_UPDATE_DURATION = 0.1f;
  // the height of the health bar which can´t be smaller than the nineslicedrawable
  private static final int HEALTH_BAR_HEIGHT = 10;
  // the width of the health bar which can´t be smaller than the nineslicedrawable
  private static final int HEALTH_BAR_WIDTH = 50;

  /** Mapping from actual entity and health bar of this entity. */
  private final Map<Integer, ProgressBar> healthBarMapping = new HashMap<>();

  /** Create a new HealthBarSystem. */
  public HealthBarSystem() {
    super(HealthComponent.class, PositionComponent.class);
    this.onEntityAdd =
        (x) -> {
          LOGGER.log(CustomLogLevel.TRACE, "HealthBarSystem got send a new Entity");
          ProgressBar newHealthBar =
              createNewHealthBar(x.fetch(PositionComponent.class).orElseThrow());
          LOGGER.log(CustomLogLevel.TRACE, "created a new health bar");
          Entity e = new Entity("HealthBar");
          LOGGER.log(CustomLogLevel.TRACE, "created a new Entity for the health bar");
          Container<ProgressBar> group = new Container<>(newHealthBar);
          // disabling layout enforcing from parent
          group.setLayoutEnabled(false);
          e.add(new UIComponent(group, false, false));
          Game.add(e);
          LOGGER.log(CustomLogLevel.TRACE, "created a new UIComponent for the health bar");
          healthBarMapping.put(x.id(), newHealthBar);
          LOGGER.log(CustomLogLevel.TRACE, "HealthBarSystem added to temporary mapping");
        };
    LOGGER.log(CustomLogLevel.TRACE, "HealthBarSystem onEntityAdd was changed");
    this.onEntityRemove = (x) -> healthBarMapping.remove(x.id()).remove();
    LOGGER.log(CustomLogLevel.TRACE, "HealthBarSystem onEntityRemove was changed");
    LOGGER.info("HealthBarSystem created");
  }

  @Override
  public void execute() {
    entityStream().map(this::buildDataObject).forEach(this::update);
  }

  private void update(final EnemyData ed) {
    // set visible only if entity lost health and if entity is visible
    ed.pb.setVisible(
        ed.dc.isVisible() && ed.hc.currentHealthpoints() != ed.hc.maximalHealthpoints());
    updatePosition(ed.pb, ed.pc);

    // set value to health percent
    ed.pb.setValue((float) ed.hc.currentHealthpoints() / ed.hc.maximalHealthpoints());
  }

  private EnemyData buildDataObject(final Entity entity) {
    return new EnemyData(
        entity.fetch(HealthComponent.class).orElseThrow(),
        entity.fetch(PositionComponent.class).orElseThrow(),
        entity.fetch(DrawComponent.class).orElseThrow(),
        healthBarMapping.get(entity.id()));
  }

  private ProgressBar createNewHealthBar(PositionComponent pc) {
    ProgressBar progressBar =
        new ProgressBar(MIN, MAX, STEP_SIZE, false, defaultSkin(), "healthbar");
    progressBar.setAnimateDuration(HEALTH_BAR_UPDATE_DURATION);
    progressBar.setSize(HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
    progressBar.setVisible(true);
    updatePosition(progressBar, pc);
    return progressBar;
  }

  /**
   * Moves the Progressbar to follow the Entity.
   *
   * @param pb WTF? .
   * @param pc WTF? .
   */
  private void updatePosition(ProgressBar pb, PositionComponent pc) {
    Point position = pc.position();
    Vector3 conveered = new Vector3(position.x, position.y, 0);
    // map Entity coordinates to window coordinates
    Vector3 screenPosition = CameraSystem.camera().project(conveered);
    // get the stage of the Game
    Stage stage = Game.stage().orElseThrow(() -> new RuntimeException("No Stage available"));
    // remap window coordinates against stage coordinates
    screenPosition.x = screenPosition.x / stage.getViewport().getScreenWidth() * stage.getWidth();
    screenPosition.y = screenPosition.y / stage.getViewport().getScreenHeight() * stage.getHeight();
    pb.setPosition(screenPosition.x, screenPosition.y);
  }

  private record EnemyData(
      HealthComponent hc, PositionComponent pc, DrawComponent dc, ProgressBar pb) {}
}

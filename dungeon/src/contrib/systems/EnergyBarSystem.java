package contrib.systems;

import static contrib.hud.UIUtils.defaultSkin;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import contrib.components.EnergyComponent;
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
 * Creates a progress bar that follows an entity and shows its current energy percentage.
 *
 * <p>Entities with {@link EnergyComponent}, {@link PositionComponent}, and {@link DrawComponent}
 * will be processed by this system.
 */
public final class EnergyBarSystem extends System {

  private static final Logger LOGGER = Logger.getLogger(EnergyBarSystem.class.getSimpleName());

  private static final float MIN = 0f;
  private static final float MAX = 1f;
  private static final float STEP_SIZE = 0.01f;
  private static final float UPDATE_DURATION = 0.1f;
  private static final int BAR_WIDTH = 50;
  private static final int BAR_HEIGHT = 10;
  private static final int OFFSET_TO_HEALTH_BAR = 40;

  /** Maps entity IDs to their energy progress bars. */
  private final Map<Integer, ProgressBar> energyBarMapping = new HashMap<>();

  /** Creates a new EnergyBarSystem. */
  public EnergyBarSystem() {
    super(DrawComponent.class, EnergyComponent.class, PositionComponent.class);

    this.onEntityAdd =
        entity -> {
          LOGGER.log(CustomLogLevel.TRACE, "Adding EnergyBar for entity " + entity);

          PositionComponent pc = entity.fetch(PositionComponent.class).orElseThrow();
          ProgressBar bar = createEnergyBar(pc);

          Entity barEntity = new Entity("EnergyBar_" + entity.id());
          Container<ProgressBar> container = new Container<>(bar);
          container.setLayoutEnabled(false);
          barEntity.add(new UIComponent(container, false, false));
          Game.add(barEntity);

          energyBarMapping.put(entity.id(), bar);
          LOGGER.log(CustomLogLevel.TRACE, "EnergyBar added to mapping for entity " + entity.id());
        };

    this.onEntityRemove =
        entity -> {
          ProgressBar bar = energyBarMapping.remove(entity.id());
          if (bar != null) bar.remove();
        };

    LOGGER.info("EnergyBarSystem created");
  }

  @Override
  public void execute() {
    filteredEntityStream().map(this::buildDataObject).forEach(this::updateBar);
  }

  private void updateBar(BarData data) {
    data.pb.setVisible(data.dc.isVisible() && data.ec.currentAmount() != data.ec.maxAmount());
    updatePosition(data.pb, data.pc);
    data.pb.setValue(data.ec.currentAmount() / data.ec.maxAmount());
  }

  private BarData buildDataObject(Entity entity) {
    return new BarData(
        entity.fetch(DrawComponent.class).orElseThrow(),
        entity.fetch(EnergyComponent.class).orElseThrow(),
        entity.fetch(PositionComponent.class).orElseThrow(),
        energyBarMapping.get(entity.id()));
  }

  private ProgressBar createEnergyBar(PositionComponent pc) {
    ProgressBar bar = new ProgressBar(MIN, MAX, STEP_SIZE, false, defaultSkin(), "energybar");
    bar.setAnimateDuration(UPDATE_DURATION);
    bar.setSize(BAR_WIDTH, BAR_HEIGHT);
    updatePosition(bar, pc);
    bar.setVisible(true);
    return bar;
  }

  /**
   * Updates the progress bar's screen position to follow the entity.
   *
   * @param bar the progress bar to update
   * @param pc the position component of the associated entity
   */
  private void updatePosition(ProgressBar bar, PositionComponent pc) {
    Point pos = pc.position();
    Vector3 worldCoords = new Vector3(pos.x(), pos.y(), 0);
    Vector3 screenCoords = CameraSystem.camera().project(worldCoords);

    Stage stage = Game.stage().orElseThrow(() -> new RuntimeException("No stage available"));
    screenCoords.x = screenCoords.x / stage.getViewport().getScreenWidth() * stage.getWidth();
    screenCoords.y = screenCoords.y / stage.getViewport().getScreenHeight() * stage.getHeight();

    bar.setPosition(screenCoords.x, screenCoords.y - OFFSET_TO_HEALTH_BAR);
  }

  private record BarData(
      DrawComponent dc, EnergyComponent ec, PositionComponent pc, ProgressBar pb) {}
}

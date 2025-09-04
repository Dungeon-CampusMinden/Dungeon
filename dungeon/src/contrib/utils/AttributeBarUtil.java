package contrib.utils;

import static contrib.hud.UIUtils.defaultSkin;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import contrib.components.UIComponent;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.systems.CameraSystem;
import core.utils.Point;
import java.util.Map;
import java.util.function.Function;

/** Utility class for managing attribute bars (health, mana, energy) for entities. */
public final class AttributeBarUtil {

  private static final float MIN = 0f;
  private static final float MAX = 1f;
  private static final float STEP_SIZE = 0.01f;
  private static final float UPDATE_DURATION = 0.1f;
  private static final int DEFAULT_BAR_WIDTH = 50;
  private static final int DEFAULT_BAR_HEIGHT = 10;

  private AttributeBarUtil() {} // Utility class, no instances

  /**
   * Creates a progress bar for the given entity and maps it in the provided map.
   *
   * @param entity the entity to attach the bar to
   * @param componentFetcher function to get the attribute component
   * @param barMapping map from entity ID to progress bar
   * @param styleName name of the progress bar style (e.g., "healthbar", "manabar")
   * @param verticalOffset vertical offset above the entity
   * @param <T> type of the attribute component
   */
  public static <T> void addBarToEntity(
      Entity entity,
      Function<Entity, T> componentFetcher,
      Map<Integer, ProgressBar> barMapping,
      String styleName,
      float verticalOffset) {
    PositionComponent pc = entity.fetch(PositionComponent.class).orElseThrow();
    ProgressBar bar = createBar(pc, styleName, verticalOffset);

    Entity barEntity = new Entity(styleName + "_" + entity.id());
    Container<ProgressBar> container = new Container<>(bar);
    container.setLayoutEnabled(false);
    barEntity.add(new UIComponent(container, false, false));
    Game.add(barEntity);

    barMapping.put(entity.id(), bar);
  }

  private static ProgressBar createBar(
      PositionComponent pc, String styleName, float verticalOffset) {
    ProgressBar bar = new ProgressBar(MIN, MAX, STEP_SIZE, false, defaultSkin(), styleName);
    bar.setAnimateDuration(UPDATE_DURATION);
    bar.setSize(DEFAULT_BAR_WIDTH, DEFAULT_BAR_HEIGHT);
    updatePosition(bar, pc, verticalOffset);
    bar.setVisible(true);
    return bar;
  }

  /**
   * Updates the position of the progress bar to follow the entity.
   *
   * @param bar the progress bar
   * @param pc position component of the entity
   * @param verticalOffset offset above the entity
   */
  public static void updatePosition(ProgressBar bar, PositionComponent pc, float verticalOffset) {
    Point pos = pc.position();
    Vector3 worldCoords = new Vector3(pos.x(), pos.y(), 0);
    Vector3 screenCoords = CameraSystem.camera().project(worldCoords);

    Stage stage = Game.stage().orElseThrow(() -> new RuntimeException("No stage available"));
    screenCoords.x = screenCoords.x / stage.getViewport().getScreenWidth() * stage.getWidth();
    screenCoords.y = screenCoords.y / stage.getViewport().getScreenHeight() * stage.getHeight();

    bar.setPosition(screenCoords.x, screenCoords.y - verticalOffset);
  }

  /**
   * Updates the value and visibility of the bar for the entity.
   *
   * @param entity the entity
   * @param componentFetcher fetches the attribute component (must provide current and max)
   * @param barMapping map from entity ID to progress bar
   * @param verticalOffset vertical offset
   * @param <T> type of the attribute component
   */
  public static <T> void updateBar(
      Entity entity,
      Function<Entity, AttributeProvider> componentFetcher,
      Map<Integer, ProgressBar> barMapping,
      float verticalOffset) {
    AttributeProvider comp = componentFetcher.apply(entity);
    ProgressBar bar = barMapping.get(entity.id());
    if (bar == null) return;

    bar.setVisible(
        entity.fetch(DrawComponent.class).map(DrawComponent::isVisible).orElse(false)
            && comp.current() != comp.max());
    updatePosition(bar, entity.fetch(PositionComponent.class).orElseThrow(), verticalOffset);
    bar.setValue(comp.current() / comp.max());
  }

  /** Simple interface to generalize components with current/max values. */
  public interface AttributeProvider {

    /**
     * Returns the current value of the attribute.
     *
     * @return the current value
     */
    float current();

    /**
     * Returns the maximum possible value of the attribute.
     *
     * @return the maximum value
     */
    float max();
  }
}

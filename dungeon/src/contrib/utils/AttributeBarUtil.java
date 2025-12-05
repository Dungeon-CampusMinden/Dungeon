package contrib.utils;

import static contrib.hud.UIUtils.defaultSkin;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogType;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.systems.CameraSystem;
import core.utils.Point;
import core.utils.logging.DungeonLogger;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;

/** Utility class for managing attribute bars (health, mana, energy) for entities. */
public final class AttributeBarUtil {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(AttributeBarUtil.class);

  // TODO: Change this logic, to allow for more than 1mil entities
  private static int NEXT_ID =
      1_000_000_000; // Start IDs for bars at a high number to avoid conflicts

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
   */
  public static void addBarToEntity(
      Entity entity,
      Function<Entity, AttributeProvider> componentFetcher,
      Map<Integer, ProgressBar> barMapping,
      String styleName,
      float verticalOffset) {
    Entity barEntity = new Entity(NEXT_ID++, styleName + "_" + entity.id());

    DialogContext context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.PROGRESS_BAR)
            .put(
                DialogContextKeys.PROGRESS_BAR,
                new ProgressBarContext(
                    entity.fetch(PositionComponent.class).orElseThrow(), styleName, verticalOffset))
            .build();
    UIComponent uiComp = new UIComponent(context, false, false, new int[] {});
    barEntity.add(uiComp);
    Game.add(barEntity);

    UIUtils.findTypeInGroup(uiComp.dialog(), ProgressBar.class)
        .ifPresentOrElse(
            bar -> {
              barMapping.put(entity.id(), bar);
              //updateBar(entity, componentFetcher, barMapping, verticalOffset);
            },
            () -> LOGGER.error("Failed to create progress bar for entity {}", entity));
  }

  /**
   * Builds a progress bar group from the given DialogContext.
   *
   * @param context the DialogContext containing the progress bar
   * @return a Group containing the progress bar
   */
  public static Group buildProgressBar(DialogContext context) {
    ProgressBarContext barContext =
        context.require(DialogContextKeys.PROGRESS_BAR, ProgressBarContext.class);
    ProgressBar bar = createBar(barContext);
    Container<ProgressBar> container = new Container<>(bar);
    container.setLayoutEnabled(false);
    return container;
  }

  private static ProgressBar createBar(ProgressBarContext barContext) {
    PositionComponent pc = barContext.pc();
    String styleName = barContext.styleName();
    float verticalOffset = barContext.verticalOffset();
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

    // debug prints
    System.out.println("Entity Position: (" + pos.x() + ", " + pos.y() + ")");
    System.out.println("Screen Coordinates: (" + screenCoords.x + ", " + screenCoords.y + ")");
    System.out.println("Bar Position Set To: (" + bar.getX() + ", " + bar.getY() + ")");
    System.out.println("Camera Viewport: Width=" + stage.getViewport().getScreenWidth() + ", Height=" + stage.getViewport().getScreenHeight());
    System.out.println("Stage Size: Width=" + stage.getWidth() + ", Height=" + stage.getHeight());
    System.out.println("-----------------------------");
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

    /*bar.setVisible(
    entity.fetch(DrawComponent.class).map(DrawComponent::isVisible).orElse(false)
        && comp.current() != comp.max());*/
    // TODO
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

  private record ProgressBarContext(PositionComponent pc, String styleName, float verticalOffset)
      implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
  }
}

package contrib.hud.utils;

import contrib.components.UIComponent;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogType;
import contrib.hud.elements.AttributeBarDialogData;
import contrib.hud.elements.AttributeBarHandle;
import contrib.hud.elements.AttributeBarHandleProvider;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.platform.Platform;
import core.utils.logging.DungeonLogger;
import java.util.Map;

/**
 * Utility class for managing attribute bars (health, mana, energy) for entities.
 *
 * <p>Attribute bars are client-side only visual elements that follow entities. They are created
 * using the standard entity ID system via {@link core.utils.EntityIdProvider}.
 */
public final class AttributeBarUtil {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(AttributeBarUtil.class);

  /** Gap between stacked bars. */
  public static final float BAR_GAP = 15f;

  private AttributeBarUtil() {}

  /**
   * Creates a progress bar for the given entity and maps it in the provided map.
   *
   * <p>The bar entity is created client-side only as a local entity using {@link
   * Entity#createLocalEntity(String)}.
   *
   * @param entity the entity to attach the bar to
   * @param barDisplayable the component providing bar data
   * @param barMapping map from component class to progress bar handle
   * @param verticalOffset vertical offset above the entity
   */
  public static void addBarToEntity(
    Entity entity,
    contrib.components.BarDisplayable barDisplayable,
    Map<Class<? extends contrib.components.BarDisplayable>, AttributeBarHandle> barMapping,
    float verticalOffset) {
    Entity barEntity = Entity.createLocalEntity(barDisplayable.barStyleName() + "_" + entity.id());

    DialogContext context =
      DialogContext.builder()
        .type(DialogType.DefaultTypes.PROGRESS_BAR)
        .center(false)
        .put(
          DialogContextKeys.PROGRESS_BAR,
          new AttributeBarDialogData(
            entity.fetch(PositionComponent.class).orElseThrow(),
            barDisplayable.barStyleName(),
            verticalOffset))
        .put(DialogContextKeys.OWNER_ENTITY, barEntity.id())
        .build();

    UIComponent uiComp = new UIComponent(context, false, false, new int[] {});
    barEntity.add(uiComp);
    Game.add(barEntity);

    uiComp.dialog()
      .flatMap(handle -> handle.unwrap(AttributeBarHandleProvider.class))
      .map(AttributeBarHandleProvider::attributeBarHandle)
      .ifPresentOrElse(
        handle -> barMapping.put(barDisplayable.getClass(), handle),
        () -> LOGGER.error("Failed to create attribute bar handle for entity {}", entity));
  }

  public static void updatePosition(
    AttributeBarHandle bar, PositionComponent pc, float verticalOffset) {
    Game.stage()
      .flatMap(stageHandle -> Platform.render().projectWorldToStage(pc.position(), stageHandle))
      .ifPresent(screenPoint -> bar.setPosition(screenPoint.x(), screenPoint.y() - verticalOffset));
  }

  /**
   * Updates the value and visibility of the bar for the entity.
   *
   * @param entity the entity
   * @param barDisplayable the component providing bar data
   * @param barMapping map from component class to progress bar handle
   * @param verticalOffset vertical offset
   */
  public static void updateBar(
    Entity entity,
    contrib.components.BarDisplayable barDisplayable,
    Map<Class<? extends contrib.components.BarDisplayable>, AttributeBarHandle> barMapping,
    float verticalOffset) {
    AttributeBarHandle bar = barMapping.get(barDisplayable.getClass());
    if (bar == null) {
      return;
    }

    bar.setVisible(
      entity.fetch(DrawComponent.class).map(DrawComponent::isVisible).orElse(false)
        && barDisplayable.current() != barDisplayable.max());

    updatePosition(bar, entity.fetch(PositionComponent.class).orElseThrow(), verticalOffset);
    bar.setValue(barDisplayable.current() / barDisplayable.max());
  }
}

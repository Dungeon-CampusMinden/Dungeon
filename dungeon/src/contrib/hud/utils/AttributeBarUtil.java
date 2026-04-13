package contrib.hud.utils;

import contrib.components.HealthComponent;
import contrib.components.ManaComponent;
import contrib.components.StaminaComponent;
import contrib.components.UIComponent;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogType;
import contrib.hud.elements.AttributeBarDialogData;
import contrib.hud.elements.AttributeBarHandle;
import contrib.hud.elements.AttributeBarHandleProvider;
import contrib.hud.elements.BarDisplayable;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.platform.Platform;
import core.utils.Point;
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
  private static final float BAR_MARGIN = 6f;

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
    BarDisplayable barDisplayable,
    Map<Class<? extends BarDisplayable>, AttributeBarHandle> barMapping,
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
        handle -> {
          barMapping.put(barDisplayable.getClass(), handle);
          updatePosition(handle, entity, verticalOffset);
          handle.setValue(barDisplayable.current() / barDisplayable.max());
          handle.setVisible(shouldShowBar(entity, barDisplayable));
        },
        () -> LOGGER.error("Failed to create progress bar for entity {}", entity));
  }

  public static void updatePosition(
    AttributeBarHandle bar, PositionComponent pc, float verticalOffset) {
    Point anchorPoint = pc.position().translate(0.5f, 1f);
    Game.stage()
      .flatMap(stageHandle -> Platform.render().projectWorldToStage(anchorPoint, stageHandle))
      .ifPresent(
        screenPoint -> bar.setPosition(screenPoint.x(), screenPoint.y() - BAR_MARGIN - verticalOffset));
  }

  public static void updatePosition(AttributeBarHandle bar, Entity entity, float verticalOffset) {
    boolean renderBelowEntity = shouldRenderBarBelowEntity(entity);
    Game.stage()
      .flatMap(stageHandle -> Platform.render().projectWorldToStage(barAnchorPoint(entity), stageHandle))
      .ifPresent(
        screenPoint ->
          bar.setPosition(
            screenPoint.x(), barScreenY(screenPoint.y(), renderBelowEntity, verticalOffset)));
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
    BarDisplayable barDisplayable,
    Map<Class<? extends BarDisplayable>, AttributeBarHandle> barMapping,
    float verticalOffset) {
    AttributeBarHandle bar = barMapping.get(barDisplayable.getClass());
    if (bar == null) {
      return;
    }

    bar.setVisible(shouldShowBar(entity, barDisplayable));

    updatePosition(bar, entity, verticalOffset);
    bar.setValue(barDisplayable.current() / barDisplayable.max());
  }

  static boolean shouldShowBar(Entity entity, BarDisplayable barDisplayable) {
    boolean entityVisible = entity.fetch(DrawComponent.class).map(DrawComponent::isVisible).orElse(false);
    if (!entityVisible) {
      return false;
    }

    if (isLocalPlayer(entity)) {
      if (barDisplayable instanceof HealthComponent healthComponent) {
        return healthComponent.wasDamaged() || healthComponent.current() != healthComponent.max();
      }
      if (barDisplayable instanceof ManaComponent manaComponent) {
        return manaComponent.wasConsumed() || manaComponent.current() != manaComponent.max();
      }
      if (barDisplayable instanceof StaminaComponent staminaComponent) {
        return staminaComponent.wasConsumed() || staminaComponent.current() != staminaComponent.max();
      }
    }

    return barDisplayable.current() != barDisplayable.max();
  }

  static Point barAnchorPoint(Entity entity) {
    PositionComponent positionComponent =
      entity.fetch(PositionComponent.class).orElseThrow();

    DrawComponent drawComponent = entity.fetch(DrawComponent.class).orElse(null);
    float width = drawComponent != null && drawComponent.getWidth() > 0f ? drawComponent.getWidth() : 1f;
    float height =
      drawComponent != null && drawComponent.getHeight() > 0f ? drawComponent.getHeight() : 1f;

    float yOffset = shouldRenderBarBelowEntity(entity) ? 0f : height;
    return positionComponent.position().translate(width * 0.5f, yOffset);
  }

  private static float barScreenY(float anchorY, boolean renderBelowEntity, float verticalOffset) {
    return renderBelowEntity
      ? anchorY + BAR_MARGIN + verticalOffset
      : anchorY - BAR_MARGIN - verticalOffset;
  }

  private static boolean shouldRenderBarBelowEntity(Entity entity) {
    return isLocalPlayer(entity);
  }

  private static boolean isLocalPlayer(Entity entity) {
    return entity.fetch(PlayerComponent.class).map(PlayerComponent::isLocal).orElse(false);
  }
}

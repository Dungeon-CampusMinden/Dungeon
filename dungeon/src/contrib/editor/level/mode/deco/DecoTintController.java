package contrib.editor.level.mode.deco;

import contrib.components.DecoComponent;
import core.Entity;
import core.components.DrawComponent;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The {@code DecoTintController} class manages the visual tinting of decorative entities
 * within an editor context.
 *
 * <p>It provides functionality to update, highlight, clear, and
 * restore the tinting of entities with various states such as "hovered", "placement", or
 * "default".
 */
final class DecoTintController {
  private static final int DECO_PREVIEW_TINT = 0xFFFFFF80;
  private static final int DECO_PREVIEW_BLOCKED_TINT = 0xFF6B6B99;
  private static final int DECO_DEFAULT_TINT = -1;
  private static final int DECO_HOVER_TINT = 0xFFFF00FF;

  private final Map<Entity, Integer> rememberedEditorTints = new IdentityHashMap<>();

  private Entity hoveredDecoEntity;

  void updatePlacementIndicator(Entity entity, boolean blocked) {
    if (entity == null) {
      return;
    }

    applyEditorTint(entity, blocked ? DECO_PREVIEW_BLOCKED_TINT : DECO_PREVIEW_TINT);
  }

  void updateHoveredDecoIndicator(Optional<Entity> hoveredDeco) {
    boolean sameEntity =
      hoveredDecoEntity != null
        && hoveredDeco.isPresent()
        && hoveredDecoEntity.equals(hoveredDeco.get());

    if (sameEntity) {
      return;
    }

    clearHoveredDecoIndicator();

    hoveredDeco.ifPresent(
      entity -> {
        hoveredDecoEntity = entity;
        applyEditorTint(entity, DECO_HOVER_TINT);
      });
  }

  void clearHoveredDecoIndicator() {
    if (hoveredDecoEntity == null) {
      return;
    }

    restoreEditorTint(hoveredDecoEntity);
    hoveredDecoEntity = null;
  }

  String currentHoveredDecoName() {
    if (hoveredDecoEntity == null) {
      return "none";
    }

    return hoveredDecoEntity
      .fetch(DecoComponent.class)
      .map(DecoComponent::type)
      .map(Enum::name)
      .orElse("unknown");
  }

  void restoreEditorTint(Entity entity) {
    if (entity == null) {
      return;
    }

    Integer originalTint = rememberedEditorTints.remove(entity);

    entity.fetch(DrawComponent.class)
      .ifPresent(dc -> dc.tintColor(originalTint != null ? originalTint : DECO_DEFAULT_TINT));
  }

  void restoreAllRememberedEditorTints() {
    for (Map.Entry<Entity, Integer> entry : new IdentityHashMap<>(rememberedEditorTints).entrySet()) {
      Entity entity = entry.getKey();
      Integer tint = entry.getValue();

      if (entity != null) {
        entity.fetch(DrawComponent.class).ifPresent(dc -> dc.tintColor(tint));
      }
    }

    rememberedEditorTints.clear();
  }

  private void applyEditorTint(Entity entity, int tint) {
    if (entity == null) {
      return;
    }

    entity.fetch(DrawComponent.class)
      .ifPresent(
        dc -> {
          rememberedEditorTints.putIfAbsent(entity, dc.tintColor());
          dc.tintColor(tint);
        });
  }
}

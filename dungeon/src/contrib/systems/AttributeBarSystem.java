package contrib.systems;

import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import contrib.components.BarDisplayable;
import contrib.components.HealthComponent;
import contrib.components.ManaComponent;
import contrib.components.StaminaComponent;
import contrib.utils.AttributeBarUtil;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.logging.DungeonLogger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A system that displays attribute bars above entities.
 *
 * <p>Entities with {@link BarDisplayable}, {@link PositionComponent}, and {@link DrawComponent}
 * will have progress bars rendered above them, showing their current attribute values relative to
 * the maximum. Bars are stacked automatically with configurable gaps, ordered by priority (lower
 * priority = closer to entity).
 *
 * <p>Bars are automatically created when entities are added to the system and removed when entities
 * are removed. The bars are updated each frame to reflect the current attribute values of the
 * entity. Component additions/removals are detected dynamically.
 */
public final class AttributeBarSystem extends System {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(AttributeBarSystem.class);

  /** Mapping from entity ID to a map of component class to progress bar. */
  private final Map<Integer, Map<Class<? extends BarDisplayable>, ProgressBar>> barMapping =
      new HashMap<>();

  /**
   * Creates a new {@code AttributeBarSystem}.
   *
   * <p>Registers listeners for entity addition and removal. When an entity with the required
   * components is added, attribute bars are created and attached. When the entity is removed, the
   * corresponding bars are removed.
   */
  public AttributeBarSystem() {
    super(AuthoritativeSide.CLIENT, DrawComponent.class, PositionComponent.class);

    this.onEntityRemove =
        entity -> {
          Map<Class<? extends BarDisplayable>, ProgressBar> entityBars =
              barMapping.remove(entity.id());
          if (entityBars != null) {
            entityBars.values().forEach(ProgressBar::remove);
          }
        };
  }

  /**
   * Updates all attribute bars for the entities managed by this system.
   *
   * <p>Each entity's {@link BarDisplayable} components are queried for current and maximum values,
   * and the corresponding progress bars are updated accordingly. Bars are stacked with automatic
   * offsets based on priority.
   */
  @Override
  public void execute() {
    filteredEntityStream().forEach(this::updateBarsForEntity);
  }

  private void updateBarsForEntity(core.Entity entity) {
    // Get current BarDisplayable components
    List<BarDisplayable> currentBars = new ArrayList<>();
    entity.fetch(HealthComponent.class).ifPresent(currentBars::add);
    entity.fetch(ManaComponent.class).ifPresent(currentBars::add);
    entity.fetch(StaminaComponent.class).ifPresent(currentBars::add);
    currentBars.sort(Comparator.comparingInt(BarDisplayable::barPriority));

    // Get existing bars for this entity
    Map<Class<? extends BarDisplayable>, ProgressBar> entityBars =
        barMapping.computeIfAbsent(entity.id(), k -> new HashMap<>());

    // Remove bars for components that no longer exist
    entityBars
        .entrySet()
        .removeIf(
            entry -> {
              boolean exists =
                  currentBars.stream().anyMatch(b -> b.getClass().equals(entry.getKey()));
              if (!exists) {
                entry.getValue().remove();
                return true;
              }
              return false;
            });

    // Add bars for new components
    for (BarDisplayable barDisplayable : currentBars) {
      Class<? extends BarDisplayable> componentClass = barDisplayable.getClass();
      if (!entityBars.containsKey(componentClass)) {
        // Calculate vertical offset based on priority
        int priority = barDisplayable.barPriority();
        float verticalOffset = priority * AttributeBarUtil.BAR_GAP;

        AttributeBarUtil.addBarToEntity(entity, barDisplayable, entityBars, verticalOffset);
        LOGGER.debug("Added {} bar for entity {}", barDisplayable.barStyleName(), entity.id());
      }
    }

    // Update all bars
    for (BarDisplayable barDisplayable : currentBars) {
      Class<? extends BarDisplayable> componentClass = barDisplayable.getClass();
      ProgressBar bar = entityBars.get(componentClass);
      if (bar != null) {
        int priority = barDisplayable.barPriority();
        float verticalOffset = priority * AttributeBarUtil.BAR_GAP;
        AttributeBarUtil.updateBar(entity, barDisplayable, entityBars, verticalOffset);
      }
    }
  }
}

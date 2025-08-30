package contrib.systems;

import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import contrib.components.ManaComponent;
import contrib.utils.AttributeBarUtil;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A system responsible for displaying mana bars above entities that have a {@link ManaComponent},
 * {@link PositionComponent}, and {@link DrawComponent}.
 *
 * <p>Each entity with these components will have a progress bar rendered above it, showing the
 * current and maximum mana of the entity. The bars are automatically created when the entity is
 * added to the system and removed when the entity is removed from the system.
 *
 * <p>The vertical position of the bar above the entity is defined by {@link #VERTICAL_OFFSET}. Mana
 * values are fetched in real-time from the {@link ManaComponent} and updated each frame in {@link
 * #execute()}.
 */
public final class ManaBarSystem extends System {

  private static final Logger LOGGER = Logger.getLogger(ManaBarSystem.class.getSimpleName());
  private static final float VERTICAL_OFFSET = 20f;

  /** Mapping from entity IDs to their corresponding mana bars. */
  private final Map<Integer, ProgressBar> barMapping = new HashMap<>();

  /**
   * Creates a new {@code ManaBarSystem}.
   *
   * <p>Registers listeners for entity addition and removal. When an entity with the required
   * components is added, a mana bar is created and attached. When the entity is removed, the
   * corresponding mana bar is removed.
   */
  public ManaBarSystem() {
    super(DrawComponent.class, ManaComponent.class, PositionComponent.class);

    this.onEntityAdd =
        entity -> {
          LOGGER.fine("Adding mana bar for entity " + entity.id());
          AttributeBarUtil.addBarToEntity(
              entity,
              e ->
                  new AttributeBarUtil.AttributeProvider() {
                    final ManaComponent mc = e.fetch(ManaComponent.class).orElseThrow();

                    @Override
                    public float current() {
                      return mc.currentAmount();
                    }

                    @Override
                    public float max() {
                      return mc.maxAmount();
                    }
                  },
              barMapping,
              "manabar",
              VERTICAL_OFFSET);
        };

    this.onEntityRemove =
        entity -> {
          ProgressBar bar = barMapping.remove(entity.id());
          if (bar != null) bar.remove();
        };

    LOGGER.info("ManaBarSystem created");
  }

  /**
   * Updates all mana bars for the entities managed by this system.
   *
   * <p>This method queries the current and maximum mana from each entity's {@link ManaComponent}
   * and updates the corresponding progress bar.
   */
  @Override
  public void execute() {
    filteredEntityStream()
        .forEach(
            entity ->
                AttributeBarUtil.updateBar(
                    entity,
                    e ->
                        new AttributeBarUtil.AttributeProvider() {
                          final ManaComponent mc = e.fetch(ManaComponent.class).orElseThrow();

                          @Override
                          public float current() {
                            return mc.currentAmount();
                          }

                          @Override
                          public float max() {
                            return mc.maxAmount();
                          }
                        },
                    barMapping,
                    VERTICAL_OFFSET));
  }
}

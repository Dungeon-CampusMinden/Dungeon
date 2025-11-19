package core.network;

import contrib.components.HealthComponent;
import contrib.components.ManaComponent;
import contrib.components.UIComponent;
import contrib.systems.PositionSync;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.SoundComponent;
import core.network.messages.c2s.RequestEntitySpawn;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.SnapshotMessage;
import core.utils.Direction;
import core.utils.logging.DungeonLogger;
import java.util.*;

/**
 * The default implementation of {@link SnapshotTranslator}.
 *
 * <p>Server-side: builds a {@link SnapshotMessage} from authoritative entities.
 *
 * <p>Client-side: applies a {@link SnapshotMessage} by dispatching granular updates via {@link
 * MessageDispatcher}.
 *
 * <p>By default, it includes entities with {@link PositionComponent} and {@link DrawComponent}, as
 * well as UI entities (with {@link UIComponent}).
 *
 * @see SnapshotTranslator
 */
public final class DefaultSnapshotTranslator implements SnapshotTranslator {
  private static final DungeonLogger LOGGER =
      DungeonLogger.getLogger(DefaultSnapshotTranslator.class);

  private long latestServerTick = -1;

  /**
   * Checks if the server tick is valid. A server tick is valid if it is non-negative and greater
   * than the latest server tick received.
   *
   * <p>To allow overflowing server ticks, we check if near Integer.MAX_VALUE, which is the maximum
   * value for a int in Java. If the server tick is close to this value, we allow lower ticks to be
   * considered valid, effectively resetting the latest server tick.
   *
   * @param serverTick the server tick to validate
   * @return true if the server tick is valid, false otherwise
   */
  private boolean isServerTickValid(int serverTick) {
    final int MAX_TICK_THRESHOLD = 1000; // Threshold to reset latestServerTick
    if (serverTick < 0) {
      return false; // Server tick must be non-negative
    }

    if (serverTick > Integer.MAX_VALUE - MAX_TICK_THRESHOLD) {
      // If server tick is near Long.MAX_VALUE, reset latestServerTick
      latestServerTick = -1;
      return true; // Allow lower ticks to be valid
    }

    if (serverTick <= latestServerTick) {
      return false; // Server tick must be greater than the latest received tick
    }

    latestServerTick = serverTick;
    return true;
  }

  // Server-side
  @Override
  public Optional<SnapshotMessage> translateToSnapshot(int serverTick) {
    if (!isServerTickValid(serverTick)) {
      LOGGER.trace("No new server tick, skipping snapshot for server tick: {}", serverTick);
      return Optional.empty(); // Skip snapshot if server tick is invalid
    }
    latestServerTick = serverTick;

    List<EntityState> list = new ArrayList<>();

    Game.levelEntities()
        .filter(this::isClientRelevant)
        .forEach(
            e -> {
              EntityState.Builder builder = EntityState.builder();
              builder.entityId(e.id());
              builder.entityName(e.name());

              // Position
              e.fetch(PositionComponent.class)
                  .ifPresent(
                      pc -> {
                        builder.position(pc.position());
                        builder.viewDirection(pc.viewDirection());
                        builder.rotation(pc.rotation());
                      });

              // Health
              e.fetch(HealthComponent.class)
                  .ifPresent(
                      hc -> {
                        builder.currentHealth(hc.currentHealthpoints());
                        builder.maxHealth(hc.maximalHealthpoints());
                      });

              // Mana
              e.fetch(contrib.components.ManaComponent.class)
                  .ifPresent(
                      mc -> {
                        builder.currentMana(mc.currentAmount());
                        builder.maxMana(mc.maxAmount());
                      });

              // Animation
              e.fetch(DrawComponent.class)
                  .ifPresent(
                      dc -> {
                        builder.stateName(dc.stateMachine().getCurrentStateName());
                        builder.tintColor(dc.tintColor());
                      });

              // Sounds
              e.fetch(SoundComponent.class)
                  .ifPresent(
                      sc -> {
                        if (!sc.sounds().isEmpty()) {
                          builder.sounds(new ArrayList<>(sc.sounds()));
                        }
                      });

              list.add(builder.build());
            });
    return Optional.of(new SnapshotMessage(serverTick, list));
  }

  private boolean isClientRelevant(Entity entity) {
    if (entity.isPresent(PositionComponent.class) && entity.isPresent(DrawComponent.class)) {
      // Normal Entity
      return true;
    }
    if (entity.isPresent(UIComponent.class)) {
      // UI Entity
      return true;
    }
    // Sound Entity
    if (entity.isPresent(SoundComponent.class)) {
      return true;
    }

    return false;
  }

  // Client-side
  @Override
  public void applySnapshot(SnapshotMessage snapshot, MessageDispatcher dispatcher) {
    if (!isServerTickValid(snapshot.serverTick())) {
      LOGGER.debug(
          "Not the latest server tick, skipping snapshot: {}, latest: {}",
          snapshot.serverTick(),
          latestServerTick);
      return;
    }
    latestServerTick = snapshot.serverTick();

    snapshot
        .entities()
        .forEach(
            snap -> {
              try {
                final int entityId = snap.entityId();
                Optional<Entity> targetEntity = Game.findEntityById(entityId);

                if (targetEntity.isEmpty()) {
                  LOGGER.warn(
                      "No entity found for snapshot with id: {}. Requesting spawn.", entityId);
                  Game.network().send((short) 0, new RequestEntitySpawn(entityId), true);
                  return;
                }

                Entity entity = targetEntity.get();
                snap.entityName().ifPresent(entity::name);

                entity
                    .fetch(PositionComponent.class)
                    .ifPresent(
                        pc -> {
                          snap.position().ifPresent(pc::position);
                          snap.viewDirection()
                              .ifPresent(
                                  viewDir -> {
                                    try {
                                      pc.viewDirection(Direction.valueOf(viewDir));
                                    } catch (IllegalArgumentException ignored) {
                                    }
                                  });
                          PositionSync.syncPosition(entity);
                        });

                entity
                    .fetch(DrawComponent.class)
                    .ifPresent(
                        dc -> {
                          snap.stateName()
                              .ifPresent(
                                  stateName ->
                                      dc.stateMachine()
                                          .setState(
                                              stateName,
                                              Direction.valueOf(
                                                  snap.viewDirection().orElse("DOWN"))));
                          snap.tintColor().ifPresent(dc::tintColor);
                        });

                entity
                    .fetch(HealthComponent.class)
                    .ifPresentOrElse(
                        hc -> snap.currentHealth().ifPresent(hc::currentHealthpoints),
                        () ->
                            snap.maxHealth()
                                .ifPresent(
                                    maxHealth -> {
                                      HealthComponent hc = new HealthComponent(maxHealth);
                                      entity.add(hc);
                                      snap.currentHealth().ifPresent(hc::currentHealthpoints);
                                    }));

                entity
                    .fetch(ManaComponent.class)
                    .ifPresentOrElse(
                        hc -> snap.currentMana().ifPresent(hc::currentAmount),
                        () ->
                            snap.maxMana()
                                .ifPresent(
                                    maxMana -> {
                                      ManaComponent mc = new ManaComponent(maxMana, maxMana, 0);
                                      entity.add(mc);
                                      snap.currentMana().ifPresent(mc::currentAmount);
                                    }));

                // Sounds
                snap.sounds()
                    .ifPresentOrElse(
                        soundSpecs -> {
                          SoundComponent sc =
                              entity
                                  .fetch(SoundComponent.class)
                                  .orElseGet(
                                      () -> {
                                        SoundComponent newSc = new SoundComponent();
                                        entity.add(newSc);
                                        return newSc;
                                      });
                          sc.replaceAll(soundSpecs);
                        },
                        () -> {
                          // No audio in snapshot, clear if present
                          entity.fetch(SoundComponent.class).ifPresent(SoundComponent::clear);
                        });
              } catch (Exception ignored) {
              }
            });
  }
}

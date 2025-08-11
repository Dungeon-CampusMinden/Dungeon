package core.network;

import contrib.components.HealthComponent;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.network.messages.EntityState;
import core.network.messages.SnapshotMessage;
import core.utils.Direction;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DefaultSnapshotTranslator implements SnapshotTranslator {
  private static final Logger LOGGER =
    LoggerFactory.getLogger(DefaultSnapshotTranslator.class.getName());

  private long latestServerTick = -1;

  /**
   * Checks if the server tick is valid. A server tick is valid if it is non-negative and greater
   * than the latest server tick received.
   *
   * <p>To allow overflowing server ticks, we check if near Long.MAX_VALUE, which is the
   * maximum value for a long in Java. If the server tick is close to this value, we allow
   * lower ticks to be considered valid, effectively resetting the latest server tick.
   *
   * @param serverTick the server tick to validate
   * @return true if the server tick is valid, false otherwise
   */
  private boolean isServerTickValid(long serverTick) {
    final int MAX_TICK_THRESHOLD = 1000; // Threshold to reset latestServerTick
    if (serverTick < 0) {
      return false; // Server tick must be non-negative
    }

    if (serverTick > Long.MAX_VALUE - MAX_TICK_THRESHOLD) {
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
  public Optional<SnapshotMessage> translateToSnapshot(long serverTick, Map<Integer, Entity> clientEntities) {
    if (!isServerTickValid(serverTick)) {
      LOGGER.warn(
        "No new server tick, skipping snapshot for server tick: {}",
        serverTick,
        latestServerTick);
      return Optional.empty(); // Skip snapshot if server tick is invalid
    }
    latestServerTick = serverTick;

    List<EntityState> list = new ArrayList<>(clientEntities.size());
    for (Map.Entry<Integer, Entity> entry : clientEntities.entrySet()) {
      Entity e = entry.getValue();
      EntityState.Builder builder = EntityState.builder();
      builder.entityName(entry.getValue().name());

      // Position
      e.fetch(PositionComponent.class)
          .ifPresent(
              pc -> {
                builder.position(pc.position());
                builder.viewDirection(pc.viewDirection());
              });

      // Health
      e.fetch(HealthComponent.class)
          .ifPresent(
              hc -> {
                builder.currentHealth(hc.currentHealthpoints());
                builder.maxHealth(hc.maximalHealthpoints());
              });

      // Animation
      e.fetch(DrawComponent.class).ifPresent(dc -> {
        builder.animation(dc.currentAnimationName());
        builder.tintColor(dc.tintColor());
      });

      list.add(builder.build());
    }
    return Optional.of(new SnapshotMessage(serverTick, list));
  }

  // Client-side
  @Override
  public void applySnapshot(SnapshotMessage snapshot, MessageDispatcher dispatcher) {
    if (!isServerTickValid(snapshot.serverTick())) {
      LOGGER.warn(
        "Not the latest server tick, skipping snapshot: "
        +  snapshot.serverTick()
        + ", latest: "
        + latestServerTick);
      return;
    }
    latestServerTick = snapshot.serverTick();

    snapshot
        .entities()
        .forEach(
            snap -> {
              try {
                final String entityName = snap.entityName();
                Optional<Entity> targetEntity = resolveEntityByName(entityName);

                if (targetEntity.isEmpty()) {
                  LOGGER.warn(
                      "No entity found for snapshot with Name: "
                          + entityName);
                  // TODO: Request entity spawn event
                  return;
                }

                Entity entity = targetEntity.get();

                entity
                    .fetch(PositionComponent.class)
                    .ifPresent(
                        pc -> {
                          pc.position(snap.position());
                          snap.viewDirection()
                              .ifPresent(
                                  viewDir -> {
                                    try {
                                      pc.viewDirection(Direction.valueOf(viewDir));
                                    } catch (IllegalArgumentException ignored) {
                                    }
                                  });
                        });

                entity
                    .fetch(DrawComponent.class)
                    .ifPresent(dc -> {
                      snap.animation().ifPresent(dc::currentAnimation);
                      snap.tintColor().ifPresent(dc::tintColor);
                    });

                entity
                    .fetch(HealthComponent.class)
                    .ifPresent(
                        hc -> {
                          snap.currentHealth().ifPresent(hc::currentHealthpoints);
                          snap.maxHealth().ifPresent(hc::maximalHealthpoints);
                        });
              } catch (Exception ignored) {
              }
            });
  }

  private static Optional<Entity> resolveEntityByName(String entityName) {
    return Game.entityStream()
        .filter(e -> e.name().equals(entityName))
        .findFirst();
  }
}

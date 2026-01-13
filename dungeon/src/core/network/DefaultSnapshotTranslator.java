package core.network;

import contrib.components.*;
import contrib.item.ItemSnapshot;
import contrib.systems.PositionSync;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.SoundComponent;
import core.level.elements.ILevel;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.network.messages.c2s.RequestEntitySpawn;
import core.network.messages.s2c.DeltaSnapshotMessage;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.LevelState;
import core.network.messages.s2c.SnapshotMessage;
import core.network.server.ClientState;
import core.utils.Direction;
import core.utils.Point;
import core.utils.logging.DungeonLogger;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The default implementation of {@link SnapshotTranslator}.
 *
 * <p>Server-side: builds a {@link SnapshotMessage} from authoritative entities. Supports both full
 * snapshots and delta snapshots for bandwidth optimization.
 *
 * <p>Client-side: applies a {@link SnapshotMessage} or {@link DeltaSnapshotMessage} by dispatching
 * granular updates via {@link MessageDispatcher}.
 *
 * <p>By default, it includes entities with {@link PositionComponent} and {@link DrawComponent}, as
 * well as UI entities (with {@link UIComponent}).
 *
 * @see SnapshotTranslator
 */
public class DefaultSnapshotTranslator implements SnapshotTranslator {
  private static final DungeonLogger LOGGER =
      DungeonLogger.getLogger(DefaultSnapshotTranslator.class);

  private static final long SPAWN_REQUEST_COOLDOWN_MS = 5000L;

  protected long latestServerTick = -1;
  protected final Map<Integer, Long> lastSpawnRequestTimes = new HashMap<>();

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
  protected boolean isServerTickValid(int serverTick) {
    final int MAX_TICK_THRESHOLD = 1000; // Threshold to reset latestServerTick
    if (serverTick < 0) {
      LOGGER.warn("Received negative server tick: {}", serverTick);
      return false; // Server tick must be non-negative
    }

    if (serverTick > Integer.MAX_VALUE - MAX_TICK_THRESHOLD) {
      // If server tick is near Long.MAX_VALUE, reset latestServerTick
      LOGGER.info(
          "Server tick near max value ({}), resetting latestServerTick from {} to -1",
          serverTick,
          latestServerTick);
      latestServerTick = -1;
      return true; // Allow lower ticks to be valid
    }

    if (serverTick <= latestServerTick) {
      LOGGER.warn(
          "Received out-of-order server tick: {}, latest: {}", serverTick, latestServerTick);
      return false; // Server tick must be greater than the latest received tick
    }

    latestServerTick = serverTick;
    return true;
  }

  // Server-side
  @Override
  public Optional<SnapshotMessage> translateToSnapshot(int serverTick) {
    if (!isServerTickValid(serverTick)) {
      LOGGER.warn("No new server tick, skipping snapshot for server tick: {}", serverTick);
      return Optional.empty(); // Skip snapshot if server tick is invalid
    }
    latestServerTick = serverTick;

    List<EntityState> list = new ArrayList<>();

    Game.levelEntities()
        .filter(this::isClientRelevant)
        .forEach(
            e -> {
              EntityState.Builder builder = createBuilder(e);
              populateBuilder(e, builder);
              list.add(builder.build());
            });
    return Optional.of(new SnapshotMessage(serverTick, list, LevelState.currentLevelStateFull()));
  }

  /**
   * Creates a new EntityState.Builder for the given entity.
   *
   * <p>Subclasses can override this method to return a custom Builder subclass for additional
   * fields.
   *
   * @param entity the entity to create a builder for
   * @return a new EntityState.Builder instance
   */
  protected EntityState.Builder createBuilder(Entity entity) {
    return EntityState.builder();
  }

  /**
   * Populates the builder with data from the entity's components.
   *
   * <p>Subclasses can override this method to add custom component data. Make sure to call {@code
   * super.populateBuilder(entity, builder)} to include the base component data.
   *
   * @param entity the entity to extract data from
   * @param builder the builder to populate
   */
  protected void populateBuilder(Entity entity, EntityState.Builder builder) {
    builder.entityId(entity.id());
    builder.entityName(entity.name());

    // Position
    entity
        .fetch(PositionComponent.class)
        .ifPresent(
            pc -> {
              builder.position(pc.position());
              builder.viewDirection(pc.viewDirection());
              builder.rotation(pc.rotation());
            });

    // Health
    entity
        .fetch(HealthComponent.class)
        .ifPresent(
            hc -> {
              builder.currentHealth(hc.currentHealthpoints());
              builder.maxHealth(hc.maximalHealthpoints());
            });

    // Mana
    entity
        .fetch(ManaComponent.class)
        .ifPresent(
            mc -> {
              builder.currentMana(mc.currentAmount());
              builder.maxMana(mc.maxAmount());
            });

    // Stamina
    entity
        .fetch(StaminaComponent.class)
        .ifPresent(
            sc -> {
              builder.currentStamina(sc.currentAmount());
              builder.maxStamina(sc.maxAmount());
            });

    // Animation
    entity
        .fetch(DrawComponent.class)
        .ifPresent(
            dc -> {
              builder.stateName(dc.stateMachine().getCurrentStateName());
              builder.tintColor(dc.tintColor());
            });

    // Sounds
    entity
        .fetch(SoundComponent.class)
        .ifPresent(
            sc -> {
              if (!sc.sounds().isEmpty()) {
                builder.sounds(sc.sounds());
              }
            });

    // Inventory; convert to ItemSnapshot for compact network transmission
    entity
        .fetch(InventoryComponent.class)
        .ifPresent(
            ic -> {
              var items = ic.items();
              var snapshots =
                  Arrays.stream(items)
                      .map(item -> item != null ? ItemSnapshot.from(item) : null)
                      .toArray(ItemSnapshot[]::new);
              builder.inventory(snapshots);
            });
  }

  /**
   * Determines if an entity is relevant for client synchronization.
   *
   * @param entity the entity to check
   * @return true if the entity should be included in snapshots
   */
  protected boolean isClientRelevant(Entity entity) {
    if (entity.isPresent(PositionComponent.class) && entity.isPresent(DrawComponent.class)) {
      // Normal Entity
      return true;
    }
    // Sound Entity
    if (entity.isPresent(SoundComponent.class)) {
      return true;
    }

    return false;
  }

  // Server-side delta generation
  @Override
  public Optional<DeltaSnapshotMessage> translateToDelta(int serverTick, ClientState client) {
    if (client == null) {
      LOGGER.warn("Cannot generate delta snapshot: client is null");
      return Optional.empty();
    }

    Map<Integer, EntityState> lastSentStates = client.lastSentEntityStates();
    Set<Integer> lastVisibleIds = client.lastVisibleEntityIds();
    Set<Integer> sentStaticIds = client.sentStaticEntityIds();
    LevelState lastLevelState = client.lastSentLevelState();

    // Get player position for proximity filtering
    Optional<Point> playerPos =
        client
            .playerEntity()
            .flatMap(e -> e.fetch(PositionComponent.class))
            .map(PositionComponent::position);

    List<EntityState> changedEntities = new ArrayList<>();
    Set<Integer> currentMobileVisibleIds = new HashSet<>();

    // Build current entity states and detect changes
    Game.levelEntities()
        .filter(this::isClientRelevant)
        .forEach(
            e -> {
              int entityId = e.id();
              boolean isRelevant = SnapshotTranslator.relevantForDelta(e);

              // Static entities: skip if already sent, they never change
              if (!isRelevant) {
                if (sentStaticIds.contains(entityId)) {
                  return; // Already sent in full snapshot, skip
                }
                // New static entity that appeared mid-level (rare case)
                // Include it in delta and mark as sent
                EntityState.Builder builder = createBuilder(e);
                populateBuilder(e, builder);
                EntityState currentState = builder.build();

                // Check proximity for new static entities too
                Optional<Point> entityPos = currentState.position();
                if (playerPos.isPresent() && entityPos.isPresent()) {
                  double distance = playerPos.get().distance(entityPos.get());
                  if (distance > 20.0) {
                    return; // Skip entities outside visibility range
                  }
                }

                changedEntities.add(currentState);
                sentStaticIds.add(entityId);
                lastSentStates.put(entityId, currentState);
                return;
              }

              // Mobile entities: full delta processing
              EntityState.Builder builder = createBuilder(e);
              populateBuilder(e, builder);
              EntityState currentState = builder.build();

              // Check if entity is near player (proximity filter)
              Optional<Point> entityPos = currentState.position();
              if (playerPos.isPresent() && entityPos.isPresent()) {
                double distance = playerPos.get().distance(entityPos.get());
                if (distance > 20.0) {
                  return; // Skip entities outside visibility range
                }
              }

              currentMobileVisibleIds.add(entityId);

              // Check if entity state has changed
              EntityState lastState = lastSentStates.get(entityId);
              if (lastState == null || !lastState.equals(currentState)) {
                changedEntities.add(currentState);
                lastSentStates.put(entityId, currentState);
              }
            });

    // Detect mobile entities that left visibility range (for ghost cleanup)
    // Only mobile entities need removal - static entities persist
    List<Integer> removedEntityIds = new ArrayList<>();
    for (Integer id : lastVisibleIds) {
      if (!currentMobileVisibleIds.contains(id)) {
        removedEntityIds.add(id);
        lastSentStates.remove(id);
      }
    }

    // Update visible entity IDs cache (only mobile entities)
    lastVisibleIds.clear();
    lastVisibleIds.addAll(currentMobileVisibleIds);

    // Generate delta level state
    LevelState deltaLevelState = LevelState.createDelta(lastLevelState);
    if (!deltaLevelState.isEmpty()) {
      // Update cached level state with full state for next comparison
      client.lastSentLevelState(LevelState.currentLevelStateFull());
    }

    // Create delta message
    DeltaSnapshotMessage delta =
        new DeltaSnapshotMessage(
            client.lastFullSnapshotTick(),
            serverTick,
            changedEntities,
            removedEntityIds,
            deltaLevelState.isEmpty() ? null : deltaLevelState);

    if (!delta.hasChanges()) {
      return Optional.empty();
    }

    return Optional.of(delta);
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
                  long now = System.currentTimeMillis();
                  long lastSent = lastSpawnRequestTimes.getOrDefault(entityId, 0L);
                  if (now - lastSent >= SPAWN_REQUEST_COOLDOWN_MS) {
                    Game.network().send((short) 0, new RequestEntitySpawn(entityId), true);
                    lastSpawnRequestTimes.put(entityId, now);
                    LOGGER.warn(
                        "No entity found for snapshot with id: {}. Requesting spawn.", entityId);
                  } else {
                    LOGGER.debug(
                        "Skipping spawn request for entity {} (cooldown active).", entityId);
                  }
                  return;
                }

                Entity entity = targetEntity.get();
                snap.entityName().ifPresent(entity::name);
                lastSpawnRequestTimes.remove(entityId);

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
                entity
                    .fetch(StaminaComponent.class)
                    .ifPresentOrElse(
                        sc -> snap.currentStamina().ifPresent(sc::currentAmount),
                        () ->
                            snap.maxStamina()
                                .ifPresent(
                                    maxStamina -> {
                                      StaminaComponent sc =
                                          new StaminaComponent(maxStamina, maxStamina, 0);
                                      entity.add(sc);
                                      snap.currentStamina().ifPresent(sc::currentAmount);
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
                          // replace all sounds and stop removed ones
                          var removedSounds = sc.replaceAll(soundSpecs);
                          removedSounds.forEach(
                              spec -> Game.audio().stopInstance(spec.instanceId()));
                        },
                        () -> {
                          // No audio in snapshot, clear if present
                          Game.audio().stopAllOnEntity(entity);
                        });

                // Inventory - convert ItemSnapshot back to Item
                snap.inventory()
                    .ifPresentOrElse(
                        snapshots -> {
                          InventoryComponent ic =
                              entity
                                  .fetch(InventoryComponent.class)
                                  .orElseGet(
                                      () -> {
                                        InventoryComponent newIc =
                                            new InventoryComponent(snapshots.length);
                                        entity.add(newIc);
                                        return newIc;
                                      });
                          ic.clear();
                          for (int i = 0; i < snapshots.length; i++) {
                            ItemSnapshot itemSnapshot = snapshots[i];
                            ic.set(i, itemSnapshot != null ? itemSnapshot.toItem() : null);
                          }
                        },
                        () -> {
                          entity
                              .fetch(InventoryComponent.class)
                              .ifPresent(InventoryComponent::clear);
                          entity.remove(InventoryComponent.class);
                        });

                // Allow subclasses to apply additional entity state
                applyEntityState(entity, snap);
              } catch (Exception e) {
                LOGGER.error(
                    "Error applying snapshot for entity id: {}: {}",
                    snap.entityId(),
                    e.getMessage(),
                    e);
              }
            });

    applyLevelState(snapshot.levelState());
  }

  /**
   * Applies additional entity state from the snapshot to the entity.
   *
   * <p>Subclasses can override this method to apply custom component data from extended EntityState
   * subclasses. The base implementation does nothing.
   *
   * @param entity the entity to apply state to
   * @param state the entity state from the snapshot
   */
  protected void applyEntityState(Entity entity, EntityState state) {
    // Base implementation does nothing; subclasses can override to apply custom state
  }

  // Client-side delta application
  @Override
  public void applyDelta(DeltaSnapshotMessage delta, MessageDispatcher dispatcher) {
    if (!isServerTickValid(delta.serverTick())) {
      LOGGER.debug(
          "Not the latest server tick, skipping delta: {}, latest: {}",
          delta.serverTick(),
          latestServerTick);
      return;
    }
    latestServerTick = delta.serverTick();

    // Apply changed entities (reuse the same logic as full snapshot)
    if (delta.changedEntities() != null) {
      for (EntityState snap : delta.changedEntities()) {
        try {
          applyEntityStateToGame(snap);
        } catch (Exception e) {
          LOGGER.error(
              "Error applying delta entity state for id: {}: {}",
              snap.entityId(),
              e.getMessage(),
              e);
        }
      }
    }

    // Remove entities that left visibility range
    if (delta.removedEntityIds() != null) {
      for (Integer entityId : delta.removedEntityIds()) {
        Game.findEntityById(entityId)
            .ifPresent(
                entity -> {
                  LOGGER.debug("Removing entity {} (left visibility range)", entityId);
                  Game.remove(entity);
                });
      }
    }

    // Apply level state delta
    if (delta.deltaLevelState() != null) {
      applyLevelState(delta.deltaLevelState());
    }
  }

  /**
   * Applies a single entity state to the game. This is the core logic shared between full snapshot
   * and delta snapshot application.
   *
   * @param snap the entity state to apply
   */
  protected void applyEntityStateToGame(EntityState snap) {
    final int entityId = snap.entityId();
    Optional<Entity> targetEntity = Game.findEntityById(entityId);

    if (targetEntity.isEmpty()) {
      long now = System.currentTimeMillis();
      long lastSent = lastSpawnRequestTimes.getOrDefault(entityId, 0L);
      if (now - lastSent >= SPAWN_REQUEST_COOLDOWN_MS) {
        Game.network().send((short) 0, new RequestEntitySpawn(entityId), true);
        lastSpawnRequestTimes.put(entityId, now);
        LOGGER.warn("No entity found for snapshot with id: {}. Requesting spawn.", entityId);
      } else {
        LOGGER.debug("Skipping spawn request for entity {} (cooldown active).", entityId);
      }
      return;
    }

    Entity entity = targetEntity.get();
    snap.entityName().ifPresent(entity::name);
    lastSpawnRequestTimes.remove(entityId);

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
                                  Direction.valueOf(snap.viewDirection().orElse("DOWN"))));
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
            mc -> snap.currentMana().ifPresent(mc::currentAmount),
            () ->
                snap.maxMana()
                    .ifPresent(
                        maxMana -> {
                          ManaComponent newMc = new ManaComponent(maxMana, maxMana, 0);
                          entity.add(newMc);
                          snap.currentMana().ifPresent(newMc::currentAmount);
                        }));

    entity
        .fetch(StaminaComponent.class)
        .ifPresentOrElse(
            sc -> snap.currentStamina().ifPresent(sc::currentAmount),
            () ->
                snap.maxStamina()
                    .ifPresent(
                        maxStamina -> {
                          StaminaComponent newSc = new StaminaComponent(maxStamina, maxStamina, 0);
                          entity.add(newSc);
                          snap.currentStamina().ifPresent(newSc::currentAmount);
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
              var removedSounds = sc.replaceAll(soundSpecs);
              removedSounds.forEach(spec -> Game.audio().stopInstance(spec.instanceId()));
            },
            () -> Game.audio().stopAllOnEntity(entity));

    // Allow subclasses to apply additional entity state
    applyEntityState(entity, snap);
  }

  /**
   * Applies the level state from the snapshot to the current level.
   *
   * @param levelState the level state to apply
   */
  protected void applyLevelState(LevelState levelState) {
    // Doors
    levelState
        .doorStates()
        .forEach(
            (coordinate, isOpen) -> {
              var doorTileOpt =
                  Game.currentLevel()
                      .flatMap(level -> level.tileAt(coordinate))
                      .filter(tile -> tile instanceof DoorTile)
                      .map(tile -> (DoorTile) tile);
              doorTileOpt.ifPresent(
                  doorTile -> {
                    if (isOpen) {
                      doorTile.open();
                    } else {
                      doorTile.close();
                    }
                  });
            });

    // Design Labels
    Map<Coordinate, Byte> designLabelBytes = levelState.designLabelBytes();
    if (designLabelBytes == null) {
      return;
    }
    AtomicBoolean updateNeeded = new AtomicBoolean(false);
    for (Map.Entry<Coordinate, Byte> entry : designLabelBytes.entrySet()) {
      Coordinate coord = entry.getKey();
      Byte labelByte = entry.getValue();
      var tileOpt = Game.currentLevel().flatMap(level -> level.tileAt(coord));
      tileOpt.ifPresent(
          tile -> {
            DesignLabel label = DesignLabel.fromByte(labelByte);
            if (tile.designLabel() != label) {
              tile.designLabel(label);
              updateNeeded.set(true);
            }
          });
    }
    if (updateNeeded.get()) Game.currentLevel().ifPresent(ILevel::refreshLevelTextures);
  }
}

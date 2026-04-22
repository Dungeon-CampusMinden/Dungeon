package core.game;

import contrib.systems.EventScheduler;
import contrib.systems.LevelTickSystem;
import core.Component;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.level.elements.ILevel;
import core.network.messages.s2c.EntityDespawnEvent;
import core.network.messages.s2c.EntitySpawnEvent;
import core.platform.Platform;
import core.systems.*;
import core.utils.EntityIdProvider;
import core.utils.EntitySystemMapper;
import core.utils.logging.DungeonLogger;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * The class responsible for managing the ECS (Entity-Component-System) in the game.
 *
 * <p>It stores the {@link System systems} and the {@link Entity entities}.
 *
 * <p>For Entity management use: {@link #add(Entity)}, {@link #remove(Entity)} or {@link
 * #removeAllEntities()}
 *
 * <p>For System management use: {@link #add(System)}, {@link #remove(Class)} or {@link
 * #removeAllSystems()}
 *
 * <p>Get access via: {@link #levelEntities()}, {@link #systems()}
 *
 * <p>All API methods can also be accessed via the {@link core.Game} class.
 */
public final class ECSManagement {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ECSManagement.class);
  private static final Map<Class<? extends System>, System> SYSTEMS = new LinkedHashMap<>();
  private static final Map<ILevel, Set<EntitySystemMapper>> LEVEL_STORAGE_MAP = new HashMap<>();
  private static Set<EntitySystemMapper> activeEntityStorage = new HashSet<>();

  private static int currentTick = 0;
  private static long lastTickNanos = -1L;

  /**
   * Set to true if a new level was loaded during the current tick. This flag is used to interrupt
   * system execution when a level change occurs.
   */
  private static boolean newLevelLoadedThisTick = false;

  static {
    LEVEL_STORAGE_MAP.put(null, activeEntityStorage);
    activeEntityStorage.add(new EntitySystemMapper());
  }

  /**
   * Initializes the default rendering and level management systems.
   *
   * <p>This method registers the fundamental systems required for the engine to operate:
   * <ul>
   *   <li>LevelSystem - Manages level loading and transitions
   *   <li>Render systems (if enabled) - Platform-specific rendering systems
   *   <li>EventScheduler - Manages scheduled events
   *   <li>LevelTickSystem - Manages level-wide tick updates
   * </ul>
   *
   * <p>This initialization is idempotent: systems are only added if not already registered.
   *
   * @param profile the system profile determining which systems to initialize (must not be null)
   */
  public static synchronized void initializeDefaultSystems(SystemProfile profile) {
    registerIfAbsent(LevelSystem.class, LevelSystem::new);

    if (profile.includeRendering()) {
      for (var binding : Platform.render().defaultRenderSystems()) {
        registerIfAbsent(binding.type(), binding.factory());
      }
    }

    registerIfAbsent(EventScheduler.class, EventScheduler::new);
    registerIfAbsent(LevelTickSystem.class, LevelTickSystem::new);
  }

  /**
   * Initializes the core gameplay systems.
   *
   * <p>This method registers the fundamental gameplay and movement systems required for entity
   * movement and input handling:
   * <ul>
   *   <li>PositionSystem - Manages entity positioning
   *   <li>VelocitySystem - Manages entity velocity
   *   <li>FrictionSystem - Applies friction to moving entities
   *   <li>MoveSystem - Handles entity movement logic
   *   <li>InputSystem (if enabled) - Processes player input
   * </ul>
   *
   * <p>This initialization is idempotent: systems are only added if not already registered.
   *
   * @param profile the system profile determining which systems to initialize (must not be null)
   * @throws IllegalArgumentException if the profile is null
   */
  public static synchronized void initializeGameplaySystems(SystemProfile profile) {
    if (profile == null) throw new IllegalArgumentException("profile must not be null");

    registerIfAbsent(PositionSystem.class, PositionSystem::new);
    registerIfAbsent(VelocitySystem.class, VelocitySystem::new);
    registerIfAbsent(FrictionSystem.class, FrictionSystem::new);
    registerIfAbsent(MoveSystem.class, MoveSystem::new);

    if (profile.includeInput()) {
      registerIfAbsent(InputSystem.class, InputSystem::new);
    }
  }

  private static void registerIfAbsent(
    Class<? extends System> type, java.util.function.Supplier<? extends System> factory) {
    if (!SYSTEMS.containsKey(type)) {
      ECSManagement.add(factory.get());
    }
  }

  /**
   * Inform each {@link System} that the given Entity has changes on component bases.
   *
   * <p>If necessary, the {@link System}s will trigger {@link System#triggerOnAdd(Entity)} or {@link
   * System#triggerOnRemove(Entity)}.
   *
   * @param entity the entity that has changes in its Component Collection.
   */
  public static void informAboutChanges(Entity entity) {
    if (levelEntities().anyMatch(entity1 -> entity1.equals(entity))) {
      activeEntityStorage.forEach(f -> f.update(entity));
      LOGGER.info(entity + " informed the Game about component changes.");
    }
  }

  /**
   * The given entity will be added to the game.
   *
   * <p>If given entity has an id already used by another entity, an {@link
   * IllegalArgumentException} will be thrown.
   *
   * <p>For each {@link System}, it will be checked if the {@link System} will process this entity.
   *
   * <p>If necessary, the {@link System} will trigger {@link System#triggerOnAdd(Entity)} .
   *
   * @param entity the entity to add.
   * @return added entity for chaining
   * @throws IllegalArgumentException if an entity with the same id already exists in the game.
   */
  public static Entity add(Entity entity) {
    // Prevent duplicate IDs for different entity instances
    boolean duplicateIdExists = allEntities().anyMatch(e -> e != entity && e.id() == entity.id());
    if (duplicateIdExists)
      throw new IllegalArgumentException(
          "An Entity with id " + entity.id() + " already exists in the game.");

    // Ensure the provider knows about this id (idempotent).
    EntityIdProvider.ensureRegistered(entity.id());

    activeEntityStorage.forEach(f -> f.add(entity));
    LOGGER.info(entity + " will be added to the Game.");

    try {
      if (Game.network().isServer()) {
        if (entity.isPresent(PositionComponent.class) && entity.isPresent(DrawComponent.class)) {
          Game.network().broadcast(new EntitySpawnEvent(entity), true);
        }
      }
    } catch (IllegalStateException e) {
      LOGGER.error("Failed to broadcast entity spawn for {}: {}", entity, e.getMessage());
      // Continue without broadcasting, for unit tests
    }

    return entity;
  }

  /**
   * The given entity will be removed from the game.
   *
   * <p>If necessary, the {@link System}s will trigger {@link System#triggerOnAdd(Entity)} .
   *
   * @param entity the entity to remove
   * @return removed entity for chaining
   */
  public static Entity remove(Entity entity) {
    activeEntityStorage.forEach(f -> f.remove(entity));
    EntityIdProvider.unregister(entity.id());
    LOGGER.info(entity + " will be removed from the Game.");

    try {
      if (Game.network().isServer()) {
        Game.network()
            .broadcast(new EntityDespawnEvent(entity.id(), "Entity removed from game"), true);
      }
    } catch (IllegalStateException e) {
      LOGGER.error("Failed to broadcast entity despawn for {}: {}", entity, e.getMessage());
      // Continue without broadcasting, for unit tests
    }

    return entity;
  }

  /**
   * Create a new {@link EntitySystemMapper} with the given filter rules.
   *
   * <p>The {@link EntitySystemMapper} will be added to {@link #activeEntityStorage}.
   *
   * <p>All entities in the empty filter (basically every entity in the game) will be tried to add
   * with {@link EntitySystemMapper#add(Entity)}.
   *
   * <p>This function will not check if an {@link EntitySystemMapper} with the same rules already
   * exists. If an {@link EntitySystemMapper} exists, it will not be replaced, and the {@link
   * EntitySystemMapper} created in this function will be lost.
   *
   * @param filter Set of Component classes that define the filter rules.
   * @return the created {@link EntitySystemMapper}.
   */
  private static EntitySystemMapper createNewEntitySystemMapper(
      Set<Class<? extends Component>> filter) {
    EntitySystemMapper mapper = new EntitySystemMapper(filter);
    activeEntityStorage.add(mapper);
    levelEntities().forEach(mapper::add);
    return mapper;
  }

  /**
   * Add a {@link System} to the game.
   *
   * <p>If a System is added to the game, the {@link System#execute} method will be called every
   * frame.
   *
   * <p>Additionally, the system will be informed about all new, changed, and removed entities.
   *
   * <p>The game can only store one system of each system type.
   *
   * @param system the System to add
   * @return an optional that contains the previous existing system of the given system class if
   *     one exists
   * @see System
   * @see Optional
   */
  public static Optional<System> add(final System system) {
    System currentSystem = SYSTEMS.get(system.getClass());
    SYSTEMS.put(system.getClass(), system);
    // add to existing filter or create new filter if no matching exists
    Optional<EntitySystemMapper> filter =
        activeEntityStorage.stream().filter(f -> f.equals(system.filterRules())).findFirst();
    filter.ifPresentOrElse(
        f -> f.add(system), () -> createNewEntitySystemMapper(system.filterRules()).add(system));
    LOGGER.info("A new {} was added to the game", system.getClass().getName());
    return Optional.ofNullable(currentSystem);
  }

  /**
   * Get the current active {@link EntitySystemMapper}.
   *
   * @return The currently active {@link EntitySystemMapper}
   */
  public static Map<ILevel, Set<EntitySystemMapper>> levelStorageMap() {
    return LEVEL_STORAGE_MAP;
  }

  /**
   * Set the current active {@link EntitySystemMapper}.
   *
   * @param entityStorage The new active {@link EntitySystemMapper}
   */
  public static void activeEntityStorage(final Set<EntitySystemMapper> entityStorage) {
    activeEntityStorage = entityStorage;
  }

  /**
   * Get all Systems.
   *
   * @return a copy of the map that stores all registered {@link System} in the game.
   */
  public static Map<Class<? extends System>, System> systems() {
    return new LinkedHashMap<>(SYSTEMS);
  }

  /**
   * If a system instance of the specified type is present, performs the given action on it.
   *
   * @param <T> the type of the system, which must extend {@link System}
   * @param s the class object of the desired system type
   * @param c the {@link Consumer} to execute with the system instance if present
   */
  @SuppressWarnings("unchecked")
  public static <T extends System> void system(Class<T> s, Consumer<T> c) {
    if (SYSTEMS.containsKey(s)) {
      c.accept((T) SYSTEMS.get(s));
    } else {
      LOGGER.warn("Tried to access system of type {}, but it is not registered.", s.getName());
    }
  }

  /** Remove all registered systems from the game. */
  public static void removeAllSystems() {
    new HashSet<>(SYSTEMS.keySet()).forEach(ECSManagement::remove);
  }

  /**
   * Use this stream if you want to iterate over all entities in the current level.
   *
   * @return a stream of all entities currently in the level
   */
  public static Stream<Entity> levelEntities() {
    return levelEntities(new HashSet<>());
  }

  /**
   * Use this stream if you want to iterate over all entities that contain the necessary Components
   * to be processed by the given system.
   *
   * @param system the system that processes the entities.
   * @return a stream of all entities currently in the game that should be processed by the given
   *     system.
   */
  public static Stream<Entity> levelEntities(final System system) {
    return levelEntities(system.filterRules());
  }

  /**
   * Use this stream if you want to iterate over all entities in the current level that contain the
   * given components.
   *
   * @param filter Set of Component classes that define the filter rules.
   * @return a stream of all entities currently in the level that contains the given components.
   */
  public static Stream<Entity> levelEntities(Set<Class<? extends Component>> filter) {
    Stream<Entity> returnStream;
    Optional<EntitySystemMapper> rf =
        activeEntityStorage.stream().filter(f -> f.equals(filter)).findFirst();

    if (rf.isEmpty()) {
      EntitySystemMapper newMapper = createNewEntitySystemMapper(filter);
      returnStream = newMapper.stream();
    } else returnStream = rf.get().stream();
    return returnStream;
  }

  /**
   * Searches the current level for the first local player character.
   *
   * <p>A player entity is defined as an entity that has a {@link PlayerComponent} with {@link
   * PlayerComponent#isLocal()} returning true.
   *
   * @return the local player character can be empty if no local player is present.
   * @see PlayerComponent
   * @see #allPlayers()
   */
  public static Optional<Entity> player() {
    return allPlayers()
        .filter(e -> e.fetch(PlayerComponent.class).map(PlayerComponent::isLocal).orElse(false))
        .findFirst();
  }

  /**
   * Searches the current level for all player characters.
   *
   * <p>A player entity is defined as an entity that has a {@link PlayerComponent}.
   *
   * <p>This includes both local and remote player characters.
   *
   * @return a stream of all player characters in the current level
   * @see PlayerComponent
   */
  public static Stream<Entity> allPlayers() {
    return levelEntities().filter(e -> e.isPresent(PlayerComponent.class));
  }

  /**
   * Remove the stored system of the given class from the game. If the System is successfully
   * removed, the {@link System#triggerOnRemove(Entity)} method of the System will be called for
   * each existing Entity associated with the removed System.
   *
   * @param system the class of the system to remove
   */
  public static void remove(final Class<? extends System> system) {
    System systemInstance = SYSTEMS.remove(system);
    if (systemInstance != null) activeEntityStorage.forEach(f -> f.remove(systemInstance));
  }

  /**
   * Remove all entities from the game.
   *
   * <p>This will also remove all entities from each system.
   */
  public static void removeAllEntities() {
    allEntities().forEach(ECSManagement::remove);
    LOGGER.info("All entities will be removed from the game.");
  }

  /**
   * Use this stream if you want to iterate over all entities in the game.
   *
   * <p>This will return <strong>all</strong> entities, not just those in the current level.
   *
   * <p>Use {@link #levelEntities()} instead if you only want the entities of the current level.
   *
   * @return a stream of all entities currently in the game
   */
  public static Stream<Entity> allEntities() {
    Set<Entity> allEntities = new HashSet<>();
    LEVEL_STORAGE_MAP
        .values()
        .forEach(
            entitySystemMappers ->
                entitySystemMappers.forEach(
                    entitySystemMapper -> entitySystemMapper.stream().forEach(allEntities::add)));

    return allEntities.stream();
  }

  /**
   * Finds the entity that contains the given component instance.
   *
   * <p>This searches across all entities in the game, not just those in the current level.
   *
   * @param component the component instance whose owning entity should be located
   * @return an {@link Optional} containing the found entity, or an empty {@code Optional} if none
   *     is found
   */
  public static Optional<Entity> findInAll(final Component component) {
    return allEntities()
        .filter(entity -> entity.fetch(component.getClass()).map(component::equals).orElse(false))
        .findFirst();
  }

  /**
   * Finds the entity that contains the given component instance.
   *
   * <p>This searches across all entities at the current level.
   *
   * @param component the component instance whose owning entity should be located
   * @return an {@link Optional} containing the found entity, or an empty {@code Optional} if none
   *     is found
   */
  public static Optional<Entity> findInLevel(final Component component) {
    return levelEntities()
        .filter(entity -> entity.fetch(component.getClass()).map(component::equals).orElse(false))
        .findFirst();
  }

  /**
   * Tries to find the given entity in the game.
   *
   * <p>This searches across all entities in the game, not just those in the current level.
   *
   * @param entity the entity to search for
   * @return {@code true} if the entity is found, {@code false} otherwise
   */
  public static boolean existInAll(Entity entity) {
    return allEntities().anyMatch(entity1 -> entity1.equals(entity));
  }

  /**
   * Tries to find the given entity in the game.
   *
   * <p>This searches in the current level.
   *
   * @param entity the entity to search for
   * @return {@code true} if the entity is found, {@code false} otherwise
   */
  public static boolean existInLevel(Entity entity) {
    return levelEntities().anyMatch(entity1 -> entity1.equals(entity));
  }

  private static boolean isAuthoritative(System.AuthoritativeSide side, System system) {
    System.AuthoritativeSide systemSide = system.authoritativeSide();
    return side == System.AuthoritativeSide.BOTH
        || systemSide == System.AuthoritativeSide.BOTH
        || systemSide == side;
  }

  /**
   * Returns the current tick number, incremented each time {@link
   * #executeOneTick(System.AuthoritativeSide)} is called.
   *
   * @return the current tick number
   */
  public static int currentTick() {
    return currentTick;
  }

  /**
   * Executes one complete game tick, calculating delta time from the last tick.
   *
   * <p>This method measures the time elapsed since the last tick and calls
   * {@link #executeOneTick(System.AuthoritativeSide, float, boolean)} with the calculated
   * delta time. Rendering systems are included in this execution.
   *
   * <p>On the first call, delta time is set to 0. Subsequent calls calculate the actual elapsed time.
   *
   * @param side the authoritative side determining which systems should execute (SERVER, CLIENT, or BOTH)
   */
  public static void executeOneTick(System.AuthoritativeSide side) {
    final long now = core.utils.Time.nowNs();
    final float deltaSeconds;
    if (lastTickNanos < 0L) {
      deltaSeconds = 0f;
    } else {
      deltaSeconds = (now - lastTickNanos) / 1_000_000_000f;
    }
    lastTickNanos = now;
    executeOneTick(side, deltaSeconds);
  }

  /**
   * Executes one complete game tick with the specified delta time.
   *
   * <p>This method executes all systems that match the specified authoritative side with the given
   * delta time. Rendering systems are included in this execution.
   *
   * @param side the authoritative side determining which systems should execute (SERVER, CLIENT, or BOTH)
   * @param deltaSeconds the elapsed time in seconds since the last tick
   */
  public static void executeOneTick(System.AuthoritativeSide side, float deltaSeconds) {
    executeOneTick(side, deltaSeconds, true);
  }

  /**
   * Executes one complete game tick with the specified delta time and rendering control.
   *
   * <p>This method executes all systems that match the specified authoritative side with the given
   * delta time. Each system's execution is scheduled based on its time-based or frame-based
   * execution criteria. If a new level is loaded during execution, the tick is interrupted early.
   *
   * <p>System execution:
   * <ul>
   *   <li>Only systems matching the authoritative side are executed
   *   <li>Each system's delta time accumulates until its execution criteria are met
   *   <li>Time-based systems execute when accumulated delta time exceeds their execution interval
   *   <li>Frame-based systems execute when their frame counter exceeds their execution interval
   *   <li>After execution, system timers and counters are reset
   * </ul>
   *
   * @param side the authoritative side determining which systems should execute (SERVER, CLIENT, or BOTH)
   * @param deltaSeconds the elapsed time in seconds since the last tick (clamped to minimum 0)
   * @param renderSystems if true, all render systems are executed; if false, rendering is skipped
   */
  public static void executeOneTick(
    System.AuthoritativeSide side, float deltaSeconds, boolean renderSystems) {

    if (!(deltaSeconds >= 0f)) deltaSeconds = 0f;

    List<System> authoritativeSystems =
      ECSManagement.systems().values().stream()
        .filter(sys -> isAuthoritative(side, sys))
        .toList();

    // Execute logic for each system.
    for (System system : authoritativeSystems) {
      if (newLevelLoadedThisTick) {
        currentTick++;
        return; // Early exit if a new level was loaded this tick.
      }

      if (system.isRunning()) {
        system.deltaTime(system.deltaTime() + deltaSeconds);
      }

      system.lastExecuteInFrames(system.lastExecuteInFrames() + 1);

      boolean dueByTime =
        system.usesTimeBasedScheduling() && system.deltaTime() >= system.executeEverySeconds();

      boolean dueByFrames =
        !system.usesTimeBasedScheduling()
          && system.lastExecuteInFrames() >= system.executeEveryXFrames();

      if (system.isRunning() && (dueByTime || dueByFrames)) {
        system.execute();
        system.lastExecuteInFrames(0);
        system.deltaTime(0f);
      }
    }

    if (renderSystems) {
      renderAll(deltaSeconds);
    }

    currentTick++;
    newLevelLoadedThisTick = false;
  }

  /**
   * Finds an entity by its unique ID.
   *
   * @param entityId The unique ID of the entity to find.
   * @return An {@link Optional} containing the found entity, or an empty {@code Optional} if no
   *     entity with the given ID exists.
   */
  public static Optional<Entity> findEntityById(int entityId) {
    return ECSManagement.allEntities().filter(e -> e.id() == entityId).findFirst();
  }

  /**
   * Renders all systems with the specified delta time.
   *
   * <p>This method calls the render method on all registered systems with the given delta time.
   * It automatically skips rendering if the game is running in headless mode or if the window
   * dimensions are invalid (width or height <= 0).
   *
   * <p>The delta time is clamped to a minimum of 0 to prevent negative values.
   *
   * @param deltaSeconds the elapsed time in seconds since the last render frame (clamped to minimum 0)
   */
  public static void renderAll(float deltaSeconds) {
    if (!(deltaSeconds >= 0f)) deltaSeconds = 0f;

    if (Game.isHeadless()) return;
    if (Game.windowHeight() <= 0 || Game.windowWidth() <= 0) return;

    final float finalDeltaSeconds = deltaSeconds;
    systems().values().forEach(system -> system.render(finalDeltaSeconds));
  }
}

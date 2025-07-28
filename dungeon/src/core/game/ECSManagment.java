package core.game;

import core.Component;
import core.Entity;
import core.System;
import core.components.PlayerComponent;
import core.level.elements.ILevel;
import core.utils.EntityIdProvider;
import core.utils.EntitySystemMapper;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
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
public final class ECSManagment {
  private static final Logger LOGGER = Logger.getLogger(ECSManagment.class.getSimpleName());
  private static final Map<Class<? extends System>, System> SYSTEMS = new LinkedHashMap<>();
  private static final Map<ILevel, Set<EntitySystemMapper>> LEVEL_STORAGE_MAP = new HashMap<>();
  private static Set<EntitySystemMapper> activeEntityStorage = new HashSet<>();

  static {
    LEVEL_STORAGE_MAP.put(null, activeEntityStorage);
    activeEntityStorage.add(new EntitySystemMapper());
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
   * <p>If given entity has an id that is already used by another entity, an {@link
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
   * @return an optional that contains the previous existing system of the given system class, if
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
    LOGGER.info("A new " + system.getClass().getName() + " was added to the game");
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
  public static <T extends System> void system(Class<T> s, Consumer<T> c) {
    if (SYSTEMS.containsKey(s)) c.accept((T) SYSTEMS.get(s));
  }

  /** Remove all registered systems from the game. */
  public static void removeAllSystems() {
    new HashSet<>(SYSTEMS.keySet()).forEach(ECSManagment::remove);
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
   * Use this stream if you want to iterate over all entities in the current level, that contain the
   * given components.
   *
   * @param filter Set of Component classes that define the filter rules.
   * @return a stream of all entities currently in the level, that contains the given components.
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
   * @return the local player character, can be empty if no local player is present.
   * @see PlayerComponent
   */
  public static Optional<Entity> hero() {
    return levelEntities().filter(e -> e.fetch(PlayerComponent.class).map(PlayerComponent::isLocalHero).orElse(false)).findFirst();
  }

  /**
   * @return a stream of all hero entities in the game.
   * @see PlayerComponent
   */
  public static Stream<Entity> allHeros() {
    return levelEntities().filter(e -> e.isPresent(PlayerComponent.class));
  }

  /**
   * Remove the stored system of the given class from the game. If the System is successfully
   * removed, the {@link System#triggerOnRemove(Entity)} method of the System will be called for
   * each existing Entity that was associated with the removed System.
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
    allEntities().forEach(ECSManagment::remove);
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
   * <p>This searches across all entities in the current level.
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
}

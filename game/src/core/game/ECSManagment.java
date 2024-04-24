package core.game;

import core.Component;
import core.Entity;
import core.System;
import core.components.PlayerComponent;
import core.level.elements.ILevel;
import core.utils.EntitySystemMapper;
import java.util.*;
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
 * <p>Get access via: {@link #entityStream()}, {@link #systems()}
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
    if (entityStream().anyMatch(entity1 -> entity1.equals(entity))) {
      activeEntityStorage.forEach(f -> f.update(entity));
      LOGGER.info("Entity: " + entity + " informed the Game about component changes.");
    }
  }

  /**
   * The given entity will be added to the game.
   *
   * <p>For each {@link System}, it will be checked if the {@link System} will process this entity.
   *
   * <p>If necessary, the {@link System} will trigger {@link System#triggerOnAdd(Entity)} .
   *
   * @param entity the entity to add.
   */
  public static void add(Entity entity) {
    activeEntityStorage.forEach(f -> f.add(entity));
    LOGGER.info("Entity: " + entity + " will be added to the Game.");
  }

  /**
   * The given entity will be removed from the game.
   *
   * <p>If necessary, the {@link System}s will trigger {@link System#triggerOnAdd(Entity)} .
   *
   * @param entity the entity to remove
   */
  public static void remove(Entity entity) {
    activeEntityStorage.forEach(f -> f.remove(entity));
    LOGGER.info("Entity: " + entity + " will be removed from the Game.");
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
    entityStream().forEach(mapper::add);
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

  /** Remove all registered systems from the game. */
  public static void removeAllSystems() {
    new HashSet<>(SYSTEMS.keySet()).forEach(ECSManagment::remove);
  }

  /**
   * Use this stream if you want to iterate over all currently active entities.
   *
   * @return a stream of all entities currently in the game
   */
  public static Stream<Entity> entityStream() {
    return entityStream(new HashSet<>());
  }

  /**
   * Use this stream if you want to iterate over all entities that contain the necessary Components
   * to be processed by the given system.
   *
   * @param system the system that processes the entities.
   * @return a stream of all entities currently in the game that should be processed by the given
   *     system.
   */
  public static Stream<Entity> entityStream(final System system) {
    return entityStream(system.filterRules());
  }

  /**
   * Use this stream if you want to iterate over all entities that contain the given components.
   *
   * @param filter Set of Component classes that define the filter rules.
   * @return a stream of all entities currently in the game that contains the given components.
   */
  public static Stream<Entity> entityStream(Set<Class<? extends Component>> filter) {
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
   * @return the player character, can be null if not initialized
   * @see Optional
   */
  public static Optional<Entity> hero() {
    return entityStream().filter(e -> e.isPresent(PlayerComponent.class)).findFirst();
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
   * Use this stream if you want to iterate over all active entities.
   *
   * <p>Use {@link #entityStream()} if you want to iterate over all active entities.
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
   * Find the entity that contains the given component instance.
   *
   * @param component Component instance where the entity is searched for.
   * @return An Optional containing the found Entity, or an empty Optional if not found.
   */
  public static Optional<Entity> find(final Component component) {
    return allEntities()
        .filter(entity -> entity.fetch(component.getClass()).map(component::equals).orElse(false))
        .findFirst();
  }
}

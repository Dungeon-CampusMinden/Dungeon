package core;

import core.utils.EntitySystemMapper;
import core.utils.logging.DungeonLogger;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A System implements specific game logic (a gameplay mechanic).
 *
 * <p>A System needs to be registered with the Game via {@link Game#add(System)}.
 *
 * <p>This class is the abstract base class for each system. It implements the basic functionality
 * each system has. For example, it allows the system to pause and unpause.
 *
 * <p>A system will iterate over each {@link Entity} with specific {@link Component}s. Only if the
 * Entity contains each needed Component, the System will execute the system logic on it.
 *
 * <p>The needed Components will be defined as constructor parameters.
 *
 * <p>The {@link Game} will add the System to a corresponding {@link EntitySystemMapper} or will
 * create a {@link EntitySystemMapper}.
 *
 * <p>If an Entity gets added or removed from a {@link EntitySystemMapper}, the {@link
 * #triggerOnAdd(Entity)} or {@link #triggerOnRemove(Entity)} will be called by the {@link
 * EntitySystemMapper}. Set the {@link #onEntityAdd} or {@link #onEntityRemove} attributes in the
 * inheriting System to implement the corresponding logic for these events.
 */
public abstract class System {
  /**
   * Determines how many frames pass between two executions of the {@link #execute()}-loop.
   *
   * <p>The value 1 means that no frames are skipped, the {@link #execute()}-loop is executed every
   * frame.
   */
  public static final int DEFAULT_EVERY_FRAME_EXECUTE = 1;

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(System.class);
  private final Set<Class<? extends Component>> filterRules;
  private final int executeEveryXFrames;
  private final AuthoritativeSide authoritativeSide;
  protected boolean run;

  /**
   * Will be called after an entity was added to the corresponding {@link EntitySystemMapper}.
   *
   * <p>Use this in your own system to implement logic that should be executed after an entity was
   * added.
   *
   * <p>This will also be triggered, if a new level was loaded or the System was added to the ECS.
   *
   * <p>The default implementation is just empty.
   */
  protected Consumer<Entity> onEntityAdd = (e) -> {};

  /**
   * Will be called after an entity was removed from the corresponding {@link EntitySystemMapper}.
   *
   * <p>Use this in your own system to implement logic that should be executed after an entity was
   * removed.
   *
   * <p>This will also be triggered, if a new level was loaded or the System was removed from the
   * ECS.
   *
   * <p>The default implementation is just empty.
   */
  protected Consumer<Entity> onEntityRemove = (e) -> {};

  private int lastExecuteInFrames = 0;

  /**
   * Create a new system.
   *
   * <p>A System needs to be registered with the Game via {@link Game#add(System)}.
   *
   * @param authSide The authoritative side the system should run on.
   * @param executeEveryXFrames how often the system should be executed 1 means every frame
   * @param filterRules Needed Component-Classes. Entities need the components to be processed by
   *     this system.
   */
  @SafeVarargs
  public System(
      AuthoritativeSide authSide,
      int executeEveryXFrames,
      Class<? extends Component>... filterRules) {
    this.executeEveryXFrames = executeEveryXFrames;
    if (filterRules != null) this.filterRules = Set.of(filterRules);
    else this.filterRules = new HashSet<>();
    run = true;
    this.authoritativeSide = authSide;
    LOGGER.debug(String.format("A new %s was created", getClass().getName()));
  }

  /**
   * Create a new system.
   *
   * <p>A System needs to be registered with the Game via {@link Game#add(System)}.
   *
   * <p>This constructor will set the system to be executed every frame.
   *
   * <p>The system will be marked as running on {@link AuthoritativeSide#SERVER server} side.
   *
   * @param filterRules Needed Component-Classes. Entities need the components to be processed by
   *     this system.
   */
  @SafeVarargs
  public System(Class<? extends Component>... filterRules) {
    this(AuthoritativeSide.SERVER, filterRules);
  }

  /**
   * Create a new system.
   *
   * <p>A System needs to be registered with the Game via {@link Game#add(System)}.
   *
   * <p>This constructor will set the system to be executed every frame.
   *
   * @param authSide The authoritative side the system should run on.
   * @param filterRules Needed Component-Classes. Entities need the components to be processed by
   *     this system.
   */
  @SafeVarargs
  public System(AuthoritativeSide authSide, Class<? extends Component>... filterRules) {
    this(authSide, DEFAULT_EVERY_FRAME_EXECUTE, filterRules);
  }

  /** Implements the functionality of the system. */
  public abstract void execute();

  /**
   * Implements the render functionality of the system.
   *
   * <p>This method can be overridden by systems that require rendering capabilities.
   *
   * @param delta the time since the last frame
   */
  public void render(final float delta) {
    // Default implementation does nothing
  }

  /**
   * Triggers the action associated with adding an Entity to this System's corresponding {@link
   * EntitySystemMapper}. This method calls the {@code onEntityAdd} Consumer, executing the logic
   * defined for when an Entity is added.
   *
   * @param entity The Entity that was added to the Filter and is being processed by this System.
   */
  public void triggerOnAdd(Entity entity) {
    onEntityAdd.accept(entity);
  }

  /**
   * Triggers the action associated with removing an Entity from this System's corresponding {@link
   * EntitySystemMapper}. This method calls the {@code onEntityRemove} Consumer, executing the logic
   * defined for when an Entity is removed.
   *
   * @param entity The Entity that was removed from the Filter and is no longer processed by this
   *     System.
   */
  public void triggerOnRemove(Entity entity) {
    onEntityRemove.accept(entity);
  }

  /**
   * Retrieves the set of Component classes that define the filter rules for this System.
   *
   * <p>The System will process Entities containing all the Components specified in the returned
   * set.
   *
   * @return A {@link Set} of {@link Class} objects representing the Component classes used for
   *     filtering Entities.
   */
  public Set<Class<? extends Component>> filterRules() {
    return new HashSet<>(filterRules);
  }

  /**
   * Set this system to the running state.
   *
   * <p>A running system will be executed.
   */
  public void run() {
    if (!run) LOGGER.debug(String.format("%s is now running", this.getClass().getName()));
    run = true;
  }

  /**
   * Set this system to the paused state.
   *
   * <p>A paused system will not be executed.
   *
   * <p>A paused system can still accept, add, and remove entities. The internal set will be
   * processed when the system is running.
   */
  public void stop() {
    if (run) LOGGER.debug(String.format("%s is now paused", this.getClass().getName()));
    run = false;
  }

  /**
   * @return true if this system is running, false if it is in paused mode
   */
  public final boolean isRunning() {
    return run;
  }

  /**
   * Provides a stream of active entities that match the specified filter rules.
   *
   * <p>This stream can be used in the {@link #execute} method to iterate over and process entities
   * that have the required components.
   *
   * @param filterRules the component classes that an entity must possess to be included in the
   *     stream. Entities must have all specified components to be processed. If this Set is empty,
   *     the Stream will contain all Entities in the Game.
   * @return a stream of active entities that meet the filter criteria and will be processed by the
   *     system.
   */
  public final Stream<Entity> filteredEntityStream(
      final Set<Class<? extends Component>> filterRules) {
    return Game.levelEntities(filterRules);
  }

  /**
   * Provides a stream of active entities that are relevant to this system.
   *
   * <p>This stream can be used in the {@link #execute} method to iterate over and process entities
   * that are managed by this system.
   *
   * @return a stream of active entities that will be processed by this system.
   */
  public final Stream<Entity> filteredEntityStream() {
    return filteredEntityStream(filterRules);
  }

  /**
   * Provides a stream of active entities that match the specified filter rules.
   *
   * <p>This stream can be used in the {@link #execute} method to iterate over and process entities
   * that have the required components.
   *
   * <p>Note: Due to method overloading, it is not possible to use no filter rules. If you do not
   * provide filter rules, the filter rules defined in the system constructor will be used. Use
   * {@link #filteredEntityStream(Set)} with an empty Set to get all entities in the game.
   *
   * @param filterRules the component classes that an entity must possess to be included in the
   *     stream. Entities must have all specified components to be processed.
   * @return a stream of active entities that meet the filter criteria and will be processed by the
   *     system.
   */
  @SafeVarargs
  public final Stream<Entity> filteredEntityStream(
      final Class<? extends Component>... filterRules) {
    return filteredEntityStream(Set.of(filterRules));
  }

  /**
   * @return the frame count the system should have between executes
   */
  public int executeEveryXFrames() {
    return executeEveryXFrames;
  }

  /**
   * @return the amount of frames the System did not execute
   */
  public int lastExecuteInFrames() {
    return lastExecuteInFrames;
  }

  /**
   * Allows updating the time the system was last executed.
   *
   * @param lastExecuteInFrames the Frames since the last execute
   */
  public void lastExecuteInFrames(int lastExecuteInFrames) {
    this.lastExecuteInFrames = lastExecuteInFrames;
  }

  /**
   * Returns the authoritative side on which the system should operate.
   *
   * <p>This information can be used to determine whether the system should run on the client,
   * server, or both sides of a networked game.
   *
   * <p>A client-side system typically handles tasks related to rendering, user input, and
   * client-specific logic. A server-side system manages game state, physics calculations, and
   * authoritative game logic. Systems that operate on both sides may handle shared logic or
   * synchronization tasks.
   *
   * @return the side on which the system operates
   */
  public AuthoritativeSide authoritativeSide() {
    return authoritativeSide;
  }

  /** Enum to represent the side on which the system operates. */
  public enum AuthoritativeSide {
    /** The system operates on the client side. */
    CLIENT,
    /** The system operates on the server side. */
    SERVER,
    /** The system operates on both client and server sides. */
    BOTH;
  }
}

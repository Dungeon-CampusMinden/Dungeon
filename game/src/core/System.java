package core;

import core.utils.EntitySystemMapper;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;
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
  /** WTF? . */
  public static final int DEFAULT_EVERY_FRAME_EXECUTE = 1;

  protected static final Logger LOGGER = Logger.getLogger(System.class.getSimpleName());
  private final Set<Class<? extends Component>> filterRules;
  private final int executeEveryXFrames;
  protected boolean run;

  /**
   * Will be called after an entity was added to the corresponding {@link EntitySystemMapper}.
   *
   * <p>Use this in your own system to implement logic that should be executed after an entity was
   * added.
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
   * <p>The default implementation is just empty.
   */
  protected Consumer<Entity> onEntityRemove = (e) -> {};

  private int lastExecuteInFrames = 0;

  /**
   * Create a new system.
   *
   * <p>A System needs to be registered with the Game via {@link Game#add(System)}.
   *
   * @param executeEveryXFrames how often the system should be executed 1 means every frame
   * @param filterRules Needed Component-Classes. Entities need the components to be processed by
   *     this system.
   */
  @SafeVarargs
  public System(int executeEveryXFrames, Class<? extends Component>... filterRules) {
    this.executeEveryXFrames = executeEveryXFrames;
    if (filterRules != null) this.filterRules = Set.of(filterRules);
    else this.filterRules = new HashSet<>();
    run = true;
    LOGGER.info(String.format("A new %s was created", getClass().getName()));
  }

  /**
   * Create a new system.
   *
   * <p>A System needs to be registered with the Game via {@link Game#add(System)}.
   *
   * @param filterRules Needed Component-Classes. Entities need the components to be processed by
   *     this system.
   */
  @SafeVarargs
  public System(Class<? extends Component>... filterRules) {
    this(DEFAULT_EVERY_FRAME_EXECUTE, filterRules);
  }

  /** Implements the functionality of the system. */
  public abstract void execute();

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
    if (!run) LOGGER.info(String.format("%s is now running", this.getClass().getName()));
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
    if (run) LOGGER.info(String.format("%s is now paused", this.getClass().getName()));
    run = false;
  }

  /**
   * @return true if this system is running, false if it is in paused mode
   */
  public final boolean isRunning() {
    return run;
  }

  /**
   * Use this Stream to iterate over all active entities for this system in the {@link #execute}
   * method.
   *
   * @return a stream of active entities that will be processed by the system
   */
  public final Stream<Entity> entityStream() {
    return Game.entityStream(this);
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
}

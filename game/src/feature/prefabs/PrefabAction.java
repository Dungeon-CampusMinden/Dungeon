package feature.prefabs;

import java.util.Objects;

/**
 * Behavior of a prefab type that runs whenever a linked {@link PrefabEvent event} is raised, e.g.
 * opening a door.
 *
 * <p>Actions are input ports: each instance stores the events it listens to in {@link
 * PrefabInstance#inputs()}. Actions run on the server. Besides the actions a prefab type declares
 * itself, every prefab has the lifecycle actions {@link Prefab#ACTIVATE}, {@link Prefab#DEACTIVATE}
 * and {@link Prefab#RESET}.
 */
public final class PrefabAction implements PrefabPort {

  private final String key;
  private final String displayName;
  private final boolean needsPlayer;
  private final Handler handler;

  private PrefabAction(String key, String displayName, boolean needsPlayer, Handler handler) {
    this.key = PrefabPorts.requireKey(key);
    this.displayName = PrefabPorts.requireText(displayName, "displayName");
    this.needsPlayer = needsPlayer;
    this.handler = handler;
  }

  /**
   * Creates an action.
   *
   * @param key stable serialized key
   * @param displayName editor-facing name
   * @param handler runs the action for one spawned instance
   * @return new action
   */
  public static PrefabAction of(String key, String displayName, Handler handler) {
    return new PrefabAction(key, displayName, false, Objects.requireNonNull(handler, "handler"));
  }

  /**
   * Creates an action that acts on a player, e.g. showing a dialog.
   *
   * <p>If the triggering event carries no player, the action runs once for every player, see {@link
   * ActionContext#players()}. The editor warns about such links.
   *
   * @param key stable serialized key
   * @param displayName editor-facing name
   * @param handler runs the action for one spawned instance
   * @return new action
   */
  public static PrefabAction forPlayer(String key, String displayName, Handler handler) {
    return new PrefabAction(key, displayName, true, Objects.requireNonNull(handler, "handler"));
  }

  /**
   * Creates a lifecycle action, whose behavior is implemented by the prefab runtime.
   *
   * @param key stable serialized key
   * @param displayName editor-facing name
   * @return new lifecycle action
   */
  static PrefabAction lifecycle(String key, String displayName) {
    return new PrefabAction(key, displayName, false, null);
  }

  @Override
  public String key() {
    return key;
  }

  @Override
  public String displayName() {
    return displayName;
  }

  @Override
  public Kind kind() {
    return Kind.ACTION;
  }

  /**
   * Returns whether this action acts on the player that caused the triggering event.
   *
   * @return true if the action needs a player
   */
  public boolean needsPlayer() {
    return needsPlayer;
  }

  /**
   * Returns whether this is one of the lifecycle actions every prefab has.
   *
   * <p>Lifecycle actions also run while an instance is inactive.
   *
   * @return true for activate, deactivate and reset
   */
  public boolean isLifecycle() {
    return handler == null;
  }

  /**
   * Runs the action.
   *
   * @param context context of the receiving instance
   */
  void run(ActionContext context) {
    if (handler == null) {
      throw new IllegalStateException("Lifecycle actions are run by the runtime");
    }
    handler.run(context);
  }

  @Override
  public String toString() {
    return key;
  }

  /** Runs an action for one spawned instance. */
  @FunctionalInterface
  public interface Handler {
    /**
     * Runs the action.
     *
     * @param context context of the receiving instance
     */
    void run(ActionContext context);
  }
}

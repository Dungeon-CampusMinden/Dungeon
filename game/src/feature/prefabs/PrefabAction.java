package feature.prefabs;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

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
  private final Predicate<PrefabInstance> needsPlayer;
  // Changes an instance to act on every player, or null if the action cannot.
  private final UnaryOperator<PrefabInstance> forEveryone;
  private final Handler handler;

  private PrefabAction(
      String key,
      String displayName,
      Predicate<PrefabInstance> needsPlayer,
      UnaryOperator<PrefabInstance> forEveryone,
      Handler handler) {
    this.key = PrefabPorts.requireKey(key);
    this.displayName = PrefabPorts.requireText(displayName, "displayName");
    this.needsPlayer = needsPlayer;
    this.forEveryone = forEveryone;
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
    return new PrefabAction(
        key, displayName, instance -> false, null, Objects.requireNonNull(handler, "handler"));
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
    return new PrefabAction(
        key, displayName, instance -> true, null, Objects.requireNonNull(handler, "handler"));
  }

  /**
   * Creates an action that acts either on the player that caused the event or on every player, as
   * chosen by a selection property of the instance, e.g. who sees a dialog.
   *
   * <p>If the triggering event carries no player, the action runs once for every player, see {@link
   * ActionContext#players()}. The editor warns about such links to instances choosing the player.
   *
   * @param key stable serialized key
   * @param displayName editor-facing name
   * @param audience selection property choosing the players
   * @param triggeringPlayer value of {@code audience} choosing the player that caused the event
   * @param everyone value of {@code audience} choosing every player
   * @param handler runs the action for one spawned instance
   * @return new action
   */
  public static PrefabAction forAudience(
      String key,
      String displayName,
      PrefabProperty<String> audience,
      String triggeringPlayer,
      String everyone,
      Handler handler) {
    Objects.requireNonNull(audience, "audience");
    Objects.requireNonNull(triggeringPlayer, "triggeringPlayer");
    Objects.requireNonNull(everyone, "everyone");
    return new PrefabAction(
        key,
        displayName,
        instance -> audience.get(instance).equals(triggeringPlayer),
        instance -> audience.set(instance, everyone),
        Objects.requireNonNull(handler, "handler"));
  }

  /**
   * Creates a lifecycle action, whose behavior is implemented by the prefab runtime.
   *
   * @param key stable serialized key
   * @param displayName editor-facing name
   * @return new lifecycle action
   */
  static PrefabAction lifecycle(String key, String displayName) {
    return new PrefabAction(key, displayName, instance -> false, null, null);
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
   * Returns whether this action acts on the player that caused the triggering event when it runs
   * for an instance.
   *
   * @param instance receiving instance
   * @return true if the action needs a player
   */
  public boolean needsPlayer(PrefabInstance instance) {
    return needsPlayer.test(instance);
  }

  /**
   * Changes an instance so that this action acts on every player instead of the player that caused
   * the triggering event, e.g. for a new link from an event without a player.
   *
   * @param instance receiving instance
   * @return the changed instance, or empty if this action has no choice of players
   */
  public Optional<PrefabInstance> forEveryone(PrefabInstance instance) {
    return forEveryone == null ? Optional.empty() : Optional.of(forEveryone.apply(instance));
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

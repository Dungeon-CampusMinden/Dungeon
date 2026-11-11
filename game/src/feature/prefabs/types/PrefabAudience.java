package feature.prefabs.types;

import engine.Entity;
import engine.Game;
import feature.prefabs.ActionContext;
import feature.prefabs.PrefabAction;
import feature.prefabs.PrefabProperty;
import java.util.List;

/** Shared choice of the players an action prefab acts on, e.g. who sees a dialog. */
final class PrefabAudience {

  /** Every player of the session. */
  static final String EVERYONE = "Everyone";

  /** Only the player that caused the triggering event, or every player if it carries none. */
  static final String TRIGGERING_PLAYER = "Triggering Player";

  private PrefabAudience() {}

  /**
   * Creates the selection property for choosing the audience.
   *
   * @param displayName editor-facing name, e.g. "Play For"
   * @param defaultValue {@link #EVERYONE} or {@link #TRIGGERING_PLAYER}
   * @return selection property storing the audience
   */
  static PrefabProperty<String> property(String displayName, String defaultValue) {
    return PrefabProperty.selection(
        "audience", displayName, defaultValue, List.of(EVERYONE, TRIGGERING_PLAYER));
  }

  /**
   * Creates an action that acts on the audience chosen by a property, see {@link #players} and
   * {@link PrefabAction#forAudience}.
   *
   * @param key stable serialized key
   * @param displayName editor-facing name
   * @param property audience property of the prefab, see {@link #property}, or another selection
   *     between {@link #EVERYONE} and {@link #TRIGGERING_PLAYER}
   * @param handler runs the action for one spawned instance
   * @return new action
   */
  static PrefabAction action(
      String key,
      String displayName,
      PrefabProperty<String> property,
      PrefabAction.Handler handler) {
    return PrefabAction.forAudience(
        key, displayName, property, TRIGGERING_PLAYER, EVERYONE, handler);
  }

  /**
   * Returns the players a running action acts on.
   *
   * @param context context of the running action
   * @param property audience property of the receiving prefab
   * @return every player, or the players of {@link ActionContext#players()}
   */
  static List<Entity> players(ActionContext context, PrefabProperty<String> property) {
    return context.value(property).equals(EVERYONE)
        ? Game.allPlayers().toList()
        : context.players();
  }
}

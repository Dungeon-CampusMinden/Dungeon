package feature.prefabs;

import engine.Game;

/** Runtime side on which a prefab is instantiated. */
public enum PrefabSide {
  /** Created by the authoritative server and synchronized through the normal entity pipeline. */
  SERVER,
  /**
   * Created only by visual clients and never synchronized.
   *
   * <p>Which client-side instances exist is still controlled by the server: runtime changes made
   * through {@link PrefabRuntime} are forwarded to every client.
   */
  CLIENT;

  /**
   * Returns the sides this game instance instantiates prefabs for.
   *
   * @return {@link #CLIENT} on multiplayer clients, both sides in singleplayer, and {@link #SERVER}
   *     on a multiplayer server
   */
  public static PrefabSide[] localSides() {
    if (Game.isMultiplayerClient()) return new PrefabSide[] {CLIENT};
    if (Game.isSingleplayer()) return new PrefabSide[] {SERVER, CLIENT};
    return new PrefabSide[] {SERVER};
  }
}

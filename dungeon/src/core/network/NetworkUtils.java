package core.network;

import core.Game;
import core.game.PreRunConfiguration;
import core.network.handler.NettyNetworkHandler;
import core.network.server.ClientState;
import core.network.server.ServerRuntime;
import core.network.server.ServerTransport;
import core.network.server.Session;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utility methods for network operations, particularly for translating between entity IDs and
 * client IDs.
 *
 * @see core.network.server.ClientState
 * @see core.network.server.Session
 */
public final class NetworkUtils {

  private NetworkUtils() {}

  /**
   * Translates entity IDs to the client IDs that control those entities.
   *
   * <p>Uses {@link ClientState#playerEntity()} to find which clients control the specified
   * entities.
   *
   * @param entityIds array of entity IDs to translate
   * @return set of client IDs controlling those entities (or the single client ID in
   *     single-player/client mode)
   */
  public static Set<Short> entityIdsToClientIds(int[] entityIds) {
    if (!Game.network().isServer() || !PreRunConfiguration.multiplayerEnabled()) {
      return Set.of(Game.network().session().clientId()); // Single-player or client mode
    }

    Set<Short> clientIds = new HashSet<>();
    Map<Short, Session> sessions = getServerSessions();
    for (int entityId : entityIds) {
      sessions
          .values()
          .forEach(
              session ->
                  session
                      .clientState()
                      .flatMap(ClientState::playerEntity)
                      .ifPresent(
                          entity -> {
                            if (entity.id() == entityId) {
                              clientIds.add(session.clientId());
                            }
                          }));
    }
    return clientIds;
  }

  /**
   * Returns all currently connected client IDs.
   *
   * @return set of all connected client IDs (may be empty)
   * @throws IllegalStateException if called when not in server mode
   */
  public static Set<Short> getAllConnectedClientIds() {
    if (!Game.network().isServer()) {
      throw new IllegalStateException(
          "getAllConnectedClientIds() can only be called in server mode");
    }

    return getServerSessions().keySet();
  }

  /**
   * Gets all server sessions by accessing ServerRuntime through NettyNetworkHandler.
   *
   * @return map of client IDs to sessions or an empty map if server not initialized
   * @throws IllegalStateException if the network handler is not a {@link NettyNetworkHandler}
   */
  private static Map<Short, Session> getServerSessions() {
    if (!(Game.network() instanceof NettyNetworkHandler handler)) {
      throw new IllegalStateException("Network handler is not NettyNetworkHandler");
    }
    return handler
        .serverRuntime()
        .map(ServerRuntime::transport)
        .map(ServerTransport::clientIdToSessionMap)
        .orElse(Map.of());
  }
}

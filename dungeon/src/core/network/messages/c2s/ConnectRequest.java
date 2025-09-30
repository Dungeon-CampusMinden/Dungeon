package core.network.messages.c2s;

import core.network.messages.NetworkMessage;

/**
 * Client→server: initial handshake request.
 *
 * <p>Temporary Java-serialized prototype to be replaced by protobuf later. Expected max size: tiny
 * (a few dozen bytes).
 *
 * @param protocolVersion The protocol version the client is using. Must match the server's version.
 * @param playerName desired player name, must be unique on server
 * @param sessionId id of previous session, or 0 if none
 * @param sessionToken token of previous session, or empty array if none
 */
public record ConnectRequest(
    short protocolVersion, String playerName, int sessionId, byte[] sessionToken)
    implements NetworkMessage {

  /**
   * Create a new ConnectRequest with no prior session.
   *
   * @param protocolVersion The protocol version the client is using. Must match the server's
   *     version.
   * @param playerName desired player name, must be unique on server
   */
  public ConnectRequest(short protocolVersion, String playerName) {
    this(protocolVersion, playerName, 0, new byte[0]);
  }
}

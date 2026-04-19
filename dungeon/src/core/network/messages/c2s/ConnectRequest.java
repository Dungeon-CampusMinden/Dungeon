package core.network.messages.c2s;

import contrib.entities.CharacterClass;
import core.network.messages.NetworkMessage;
import java.util.Optional;

/**
 * Client→server: initial handshake request.
 *
 * <p>Encoded with protobuf; expected size: tiny (a few dozen bytes).
 *
 * @param protocolVersion The protocol version the client is using. Must match the server's version.
 * @param playerName desired player name, must be unique on server
 * @param sessionId id of previous session, or 0 if none
 * @param sessionToken token of previous session, or empty array if none
 * @param characterClass requested character class, or empty to use the server default
 */
public record ConnectRequest(
    short protocolVersion,
    String playerName,
    int sessionId,
    byte[] sessionToken,
    Optional<CharacterClass> characterClass)
    implements NetworkMessage {

  /**
   * Creates a new connect request.
   *
   * @param protocolVersion The protocol version the client is using. Must match the server's
   *     version.
   * @param playerName desired player name, must be unique on server
   * @param sessionId id of previous session, or 0 if none
   * @param sessionToken token of previous session, or empty array if none
   * @param characterClass requested character class, or empty to use the server default
   */
  public ConnectRequest {
    sessionToken = sessionToken == null ? new byte[0] : sessionToken.clone();
    characterClass = characterClass == null ? Optional.empty() : characterClass;
  }

  /**
   * Create a new ConnectRequest with no prior session.
   *
   * @param protocolVersion The protocol version the client is using. Must match the server's
   *     version.
   * @param playerName desired player name, must be unique on server
   */
  public ConnectRequest(short protocolVersion, String playerName) {
    this(protocolVersion, playerName, Optional.empty());
  }

  /**
   * Create a new ConnectRequest with no prior session.
   *
   * @param protocolVersion The protocol version the client is using. Must match the server's
   *     version.
   * @param playerName desired player name, must be unique on server
   * @param characterClass requested character class, or empty to use the server default
   */
  public ConnectRequest(
      short protocolVersion, String playerName, Optional<CharacterClass> characterClass) {
    this(protocolVersion, playerName, 0, new byte[0], characterClass);
  }

  /**
   * Create a new ConnectRequest with a prior session.
   *
   * @param protocolVersion The protocol version the client is using. Must match the server's
   *     version.
   * @param playerName desired player name, must be unique on server
   * @param sessionId id of previous session, or 0 if none
   * @param sessionToken token of previous session, or empty array if none
   */
  public ConnectRequest(
      short protocolVersion, String playerName, int sessionId, byte[] sessionToken) {
    this(protocolVersion, playerName, sessionId, sessionToken, Optional.empty());
  }
}

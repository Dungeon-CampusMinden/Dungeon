package engine.network.messages.c2s;

import engine.network.messages.NetworkMessage;

/**
 * Client→server: requests the character choices available for a named player before connecting.
 *
 * @param protocolVersion protocol version used by the client
 * @param playerName desired player name
 */
public record CharacterOptionsRequest(short protocolVersion, String playerName)
    implements NetworkMessage {}

package core.network.messages.client2server;

import core.network.messages.NetworkMessage;

/** Record representing a command to interact with the world. */
public record InteractCommand() implements NetworkMessage {}

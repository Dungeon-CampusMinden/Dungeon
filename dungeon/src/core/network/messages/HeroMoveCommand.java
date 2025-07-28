package core.network.messages;

import core.utils.Direction;

/**
 * Record representing a hero movement command.
 *
 * @param direction The direction of movement.
 */
public record HeroMoveCommand(Direction direction) implements NetworkMessage {}

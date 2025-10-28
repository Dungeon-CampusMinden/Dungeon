package core.network.messages.client2server;

import core.network.messages.NetworkMessage;
import core.utils.Point;

/**
 * Record representing a command to use a skill.
 *
 * @param skillIndex The index or identifier of the skill.
 * @param targetPoint Optional target point for the skill.
 */
public record UseSkillCommand(int skillIndex, Point targetPoint) implements NetworkMessage {}

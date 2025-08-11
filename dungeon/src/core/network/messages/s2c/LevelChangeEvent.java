package core.network.messages.s2c;

import core.network.messages.NetworkMessage;
import core.utils.Point;

public record LevelChangeEvent(String levelName, Point spawnPoint) implements NetworkMessage {}

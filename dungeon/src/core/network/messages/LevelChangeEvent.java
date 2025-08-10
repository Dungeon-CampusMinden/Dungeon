package core.network.messages;

import core.utils.Point;

public record LevelChangeEvent(String levelName,
                               Point spawnPoint
                               ) implements NetworkMessage {
}

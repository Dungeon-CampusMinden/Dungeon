package contrib.utils.multiplayer.packages.serializer.gamesession;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.multiplayer.packages.GameStateUpdate;
import core.Entity;

import java.util.HashSet;
import java.util.Set;

/**
 * Custom serializer to send and retrieve objects of {@link GameStateUpdate}.
 */
public class GameStateUpdateSerializer extends Serializer<GameStateUpdate> {
    @Override
    public void write(Kryo kryo, Output output, GameStateUpdate object) {
        kryo.writeObject(output, object.entities());
    }

    @Override
    public GameStateUpdate read(Kryo kryo, Input input, Class<GameStateUpdate> type) {
        final Set<Entity> entities = kryo.readObject(input, HashSet.class);
        return new GameStateUpdate(entities);
    }
}

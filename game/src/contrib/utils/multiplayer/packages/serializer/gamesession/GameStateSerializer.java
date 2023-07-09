package contrib.utils.multiplayer.packages.serializer.gamesession;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.multiplayer.packages.GameState;
import core.Entity;
import core.level.elements.ILevel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Custom serializer to send and retrieve objects of {@link GameState}.
 */
public class GameStateSerializer extends Serializer<GameState> {
    @Override
    public void write(Kryo kryo, Output output, GameState object) {
        kryo.writeObject(output, object.level());
        kryo.writeObject(output, object.entities());
        kryo.writeObject(output, object.heroesByClientId());
    }

    @Override
    public GameState read(Kryo kryo, Input input, Class<GameState> type) {
        final ILevel level = kryo.readObject(input, ILevel.class);
        final Set<Entity> entities = kryo.readObject(input, HashSet.class);
        final HashMap<Integer, Entity> heroesByClientId = kryo.readObject(input, HashMap.class);
        return new GameState(level, entities, heroesByClientId);
    }
}

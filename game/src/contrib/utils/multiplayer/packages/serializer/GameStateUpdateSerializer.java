package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.multiplayer.packages.GameStateUpdate;
import core.Entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GameStateUpdateSerializer extends Serializer<GameStateUpdate> {
    @Override
    public void write(Kryo kryo, Output output, GameStateUpdate object) {
        kryo.writeObject(output, object.entities());
        kryo.writeObject(output, object.heroesByClientId());
    }

    @Override
    public GameStateUpdate read(Kryo kryo, Input input, Class<GameStateUpdate> type) {
        final Set<Entity> entities = kryo.readObject(input, HashSet.class);
        final HashMap<Integer, Entity> heroesByClientId = kryo.readObject(input, HashMap.class);
        return new GameStateUpdate(entities, heroesByClientId);
    }
}

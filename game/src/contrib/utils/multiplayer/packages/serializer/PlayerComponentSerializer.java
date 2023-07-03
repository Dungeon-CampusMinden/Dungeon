package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.components.PlayerComponent;

public class PlayerComponentSerializer extends Serializer<PlayerComponent> {
    @Override
    public void write(Kryo kryo, Output output, PlayerComponent object) {

    }

    @Override
    public PlayerComponent read(Kryo kryo, Input input, Class<PlayerComponent> type) {
        return null;
    }
}

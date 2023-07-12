package contrib.utils.multiplayer.network.packages.serializer.components;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import core.components.PlayerComponent;

/** Custom serializer to send and retrieve objects of {@link PlayerComponent}. */
public class PlayerComponentSerializer extends Serializer<PlayerComponent> {
    @Override
    public void write(Kryo kryo, Output output, PlayerComponent object) {}

    @Override
    public PlayerComponent read(Kryo kryo, Input input, Class<PlayerComponent> type) {
        return null;
    }
}

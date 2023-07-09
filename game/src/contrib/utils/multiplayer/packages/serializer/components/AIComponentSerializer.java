package contrib.utils.multiplayer.packages.serializer.components;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.components.AIComponent;
import core.Entity;
import core.components.PlayerComponent;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Custom serializer to send and retrieve objects of {@link AIComponent}.
 */
public class AIComponentSerializer extends Serializer<AIComponent> {

    @Override
    public void write(Kryo kryo, Output output, AIComponent object) {

    }

    @Override
    public AIComponent read(Kryo kryo, Input input, Class<AIComponent> type) {
        return null;
    }
}

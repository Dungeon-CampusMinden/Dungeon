package contrib.utils.multiplayer.network.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.utils.components.interaction.DefaultInteraction;

import java.util.function.Consumer;

/** Custom serializer to send and retrieve objects of {@link DefaultInteraction}. */
public class DefaultInteractionSerializer extends Serializer<DefaultInteraction> {
    @Override
    public void write(Kryo kryo, Output output, DefaultInteraction object) {
        Class<? extends Consumer> concreteClass = object.getClass();
        kryo.writeClass(output, concreteClass);
    }

    @Override
    public DefaultInteraction read(Kryo kryo, Input input, Class<DefaultInteraction> type) {
        return new DefaultInteraction();
    }
}

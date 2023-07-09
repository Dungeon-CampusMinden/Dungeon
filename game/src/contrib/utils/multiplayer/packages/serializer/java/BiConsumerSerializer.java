package contrib.utils.multiplayer.packages.serializer.java;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.utils.components.draw.Animation;

import java.util.function.BiConsumer;

/**
 * Custom serializer to send and retrieve objects of {@link BiConsumer}.
 */
public class BiConsumerSerializer extends Serializer<BiConsumer> {
    @Override
    public void write(Kryo kryo, Output output, BiConsumer object) {

    }

    @Override
    public BiConsumer read(Kryo kryo, Input input, Class<BiConsumer> type) {
        Class<? extends BiConsumer> concreteClass = kryo.readClass(input).getType();
        return kryo.readObject(input, concreteClass);
    }
}

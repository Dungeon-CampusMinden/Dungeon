package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.function.BiConsumer;

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

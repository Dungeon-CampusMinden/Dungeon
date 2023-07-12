package contrib.utils.multiplayer.network.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.utils.components.health.DropLoot;

import java.util.function.Consumer;

/** Custom serializer to send and retrieve objects of {@link DropLoot}. */
public class DropLootSerializer extends Serializer<DropLoot> {
    @Override
    public void write(Kryo kryo, Output output, DropLoot object) {
        Class<? extends Consumer> concreteClass = object.getClass();
        kryo.writeClass(output, concreteClass);
    }

    @Override
    public DropLoot read(Kryo kryo, Input input, Class<DropLoot> type) {
        return new DropLoot();
    }
}

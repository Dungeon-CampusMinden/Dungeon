package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.utils.components.interaction.DropItemsInteraction;

import java.util.function.Consumer;

/** Custom serializer to send and retrieve objects of {@link DropItemsInteraction}. */
public class DropItemsInteractionSerializer extends Serializer<DropItemsInteraction> {
    @Override
    public void write(Kryo kryo, Output output, DropItemsInteraction object) {
        Class<? extends Consumer> concreteClass = object.getClass();
        kryo.writeClass(output, concreteClass);
    }

    @Override
    public DropItemsInteraction read(Kryo kryo, Input input, Class<DropItemsInteraction> type) {
        return new DropItemsInteraction();
    }
}

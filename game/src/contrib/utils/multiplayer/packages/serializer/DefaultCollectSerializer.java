package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.utils.components.item.DefaultCollect;

/** Custom serializer to send and retrieve objects of {@link DefaultCollect}. */
public class DefaultCollectSerializer extends Serializer<DefaultCollect> {
    @Override
    public void write(Kryo kryo, Output output, DefaultCollect object) {
        kryo.writeClass(output, object.getClass());
    }

    @Override
    public DefaultCollect read(Kryo kryo, Input input, Class<DefaultCollect> type) {
        return new DefaultCollect();
    }
}

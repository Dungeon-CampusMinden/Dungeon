package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.utils.components.draw.Animation;

/**
 * Custom serializer to send and retrieve objects of {@link contrib.utils.components.interaction.ControlPointReachable}.
 */
public class ControlPointReachableSerializer extends Serializer {
    @Override
    public void write(Kryo kryo, Output output, Object object) {
        kryo.writeClass(output, object.getClass());
    }

    @Override
    public Object read(Kryo kryo, Input input, Class type) {
        return null;
    }
}

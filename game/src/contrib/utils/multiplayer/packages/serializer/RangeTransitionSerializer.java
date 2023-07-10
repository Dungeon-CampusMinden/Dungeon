package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.utils.components.ai.transition.RangeTransition;

/** Custom serializer to send and retrieve objects of {@link RangeTransition}. */
public class RangeTransitionSerializer extends Serializer<RangeTransition> {
    @Override
    public void write(Kryo kryo, Output output, RangeTransition object) {
        kryo.writeClass(output, object.getClass());
        output.writeFloat(object.getRange());
    }

    @Override
    public RangeTransition read(Kryo kryo, Input input, Class<RangeTransition> type) {
        return new RangeTransition(input.readFloat());
    }
}

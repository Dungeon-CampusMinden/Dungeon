package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

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

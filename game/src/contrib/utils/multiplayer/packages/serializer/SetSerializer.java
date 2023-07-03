package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.HashSet;
import java.util.Set;

public class SetSerializer extends Serializer<Set<?>> {
    @Override
    public void write(Kryo kryo, Output output, Set<?> object) {
        output.writeInt(object.size());
        for (Object element : object) {
            kryo.writeClassAndObject(output, element);
        }
    }

    @Override
    public Set<?> read(Kryo kryo, Input input, Class<Set<?>> type) {
        int size = input.readInt();
        Set<Object> set = new HashSet<>();
        for (int i = 0; i < size; i++) {
            Object element = kryo.readClassAndObject(input);
            set.add(element);
        }
        return set;
    }
}

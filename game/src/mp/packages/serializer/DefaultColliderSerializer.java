package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.collision.DefaultCollider;

public class DefaultColliderSerializer extends Serializer<DefaultCollider> {
    @Override
    public void write(Kryo kryo, Output output, DefaultCollider object) {
        kryo.writeClass(output, object.getClass());
        output.writeString(object.message());
    }

    @Override
    public DefaultCollider read(Kryo kryo, Input input, Class<DefaultCollider> type) {
        return new DefaultCollider(input.readString());
    }
}

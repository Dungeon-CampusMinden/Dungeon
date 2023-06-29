package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.item.DefaultCollect;
import contrib.utils.components.item.IOnCollect;

public class IOnCollectSerializer extends Serializer<IOnCollect> {
    @Override
    public void write(Kryo kryo, Output output, IOnCollect object) {

    }

    @Override
    public IOnCollect read(Kryo kryo, Input input, Class<IOnCollect> type) {
        // Read DefaultCollect because it is the only implementation of IOnCollect yet
        return kryo.readObject(input, DefaultCollect.class);
    }
}

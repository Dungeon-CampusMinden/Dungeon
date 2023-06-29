package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.item.DefaultDrop;
import contrib.utils.components.item.IOnDrop;
public class IOnDropSerializer extends Serializer<IOnDrop> {
    @Override
    public void write(Kryo kryo, Output output, IOnDrop object) {

    }

    @Override
    public IOnDrop read(Kryo kryo, Input input, Class<IOnDrop> type) {
        // Read DefaultDrop because it is the only implementation of IOnDrop yet
        return kryo.readObject(input, DefaultDrop.class);
    }
}

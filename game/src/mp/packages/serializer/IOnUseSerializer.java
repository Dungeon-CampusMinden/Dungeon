package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.item.DefaultUseCallback;
import contrib.utils.components.item.IOnUse;

public class IOnUseSerializer extends Serializer<IOnUse> {
    @Override
    public void write(Kryo kryo, Output output, IOnUse object) {

    }

    @Override
    public IOnUse read(Kryo kryo, Input input, Class<IOnUse> type) {
        // Read DefaultUseCallback because it is the only implementation of IOnUse yet
        return kryo.readObject(input, DefaultUseCallback.class);
    }
}

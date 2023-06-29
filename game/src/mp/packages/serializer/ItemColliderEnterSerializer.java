package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.item.ItemColliderEnter;
import contrib.utils.components.item.ItemData;

public class ItemColliderEnterSerializer extends Serializer<ItemColliderEnter> {
    @Override
    public void write(Kryo kryo, Output output, ItemColliderEnter object) {
        kryo.writeClass(output, object.getClass());
        kryo.writeObject(output, object.getWhich());
    }

    @Override
    public ItemColliderEnter read(Kryo kryo, Input input, Class<ItemColliderEnter> type) {
        return new ItemColliderEnter(kryo.readObject(input, ItemData.class));
    }
}

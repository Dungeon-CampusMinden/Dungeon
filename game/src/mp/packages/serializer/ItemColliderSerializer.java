package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.collision.ItemCollider;
import contrib.utils.components.item.ItemData;

public class ItemColliderSerializer extends Serializer<ItemCollider> {
    @Override
    public void write(Kryo kryo, Output output, ItemCollider object) {
        kryo.writeClass(output, object.getClass());
        kryo.writeObject(output, object.getWhich());
    }

    @Override
    public ItemCollider read(Kryo kryo, Input input, Class<ItemCollider> type) {
        return new ItemCollider(kryo.readObject(input, ItemData.class));
    }
}

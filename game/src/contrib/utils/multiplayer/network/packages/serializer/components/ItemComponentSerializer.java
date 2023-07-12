package contrib.utils.multiplayer.network.packages.serializer.components;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.components.ItemComponent;
import contrib.utils.components.item.ItemData;

import core.Entity;

/** Custom serializer to send and retrieve objects of {@link ItemComponent}. */
public class ItemComponentSerializer extends Serializer<ItemComponent> {
    private Entity entity;

    /** Create new serializer for {@link ItemComponent}. */
    public ItemComponentSerializer() {
        super();
    }

    /**
     * Create new serializer for {@link ItemComponent}.
     *
     * @param e Entity which component should be assigned to.
     */
    public ItemComponentSerializer(Entity e) {
        this();
        entity = e;
    }

    @Override
    public void write(Kryo kryo, Output output, ItemComponent object) {
        kryo.writeObject(output, object.itemData());
    }

    @Override
    public ItemComponent read(Kryo kryo, Input input, Class<ItemComponent> type) {
        ItemData itemData = kryo.readObject(input, ItemData.class);
        return new ItemComponent(entity, itemData);
    }
}

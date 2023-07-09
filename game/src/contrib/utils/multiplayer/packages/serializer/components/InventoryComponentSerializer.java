package contrib.utils.multiplayer.packages.serializer.components;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.components.InventoryComponent;
import contrib.utils.components.item.ItemData;
import core.Entity;
import core.utils.components.draw.Animation;

import java.util.List;

/**
 * Custom serializer to send and retrieve objects of {@link InventoryComponent}.
 */
public class InventoryComponentSerializer extends Serializer<InventoryComponent> {
    private Entity entity;

    public InventoryComponentSerializer(){
        super();
    }

    public InventoryComponentSerializer(Entity e){
        super();
        entity = e;
    }
    @Override
    public void write(Kryo kryo, Output output, InventoryComponent object) {
        output.writeInt(object.maxSize());
        List<ItemData> inventory = object.items();
        output.writeInt(inventory.size());
        for (ItemData item : inventory){
            kryo.writeObject(output, item);
        }
    }

    @Override
    public InventoryComponent read(Kryo kryo, Input input, Class<InventoryComponent> type) {
        int maxSize = input.readInt();
        int size = input.readInt();
        InventoryComponent invComp = new InventoryComponent(entity, maxSize);
        for (int i = 0; i < size; i++) {
            ItemData item = kryo.readObject(input, ItemData.class);
            invComp.addItem(item);
        }
        return invComp;
    }
}

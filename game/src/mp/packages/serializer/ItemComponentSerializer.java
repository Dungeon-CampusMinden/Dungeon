package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.components.ItemComponent;
import contrib.utils.components.item.ItemData;
import core.Entity;

public class ItemComponentSerializer extends Serializer<ItemComponent> {
    private Entity entity;

    public ItemComponentSerializer(){
        super();
    }

    public ItemComponentSerializer(Entity e){
        super();
        entity = e;
    }
    @Override
    public void write(Kryo kryo, Output output, ItemComponent object) {
        kryo.writeObject(output, object.getItemData());
    }

    @Override
    public ItemComponent read(Kryo kryo, Input input, Class<ItemComponent> type) {
        ItemData itemData = kryo.readObject(input, ItemData.class);
        return new ItemComponent(entity);
    }
}

package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.item.ItemData;
import contrib.utils.components.item.ItemType;
import contrib.utils.components.stats.DamageModifier;
import core.Entity;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.components.draw.Animation;

import java.util.function.BiConsumer;

public class ItemDataSerializer extends Serializer<ItemData> {
    @Override
    public void write(Kryo kryo, Output output, ItemData object) {
        kryo.writeObject(output, object.itemType());
        kryo.writeObject(output, object.inventoryTexture());
        kryo.writeObject(output, object.worldTexture());
        output.writeString(object.itemName());
        output.writeString(object.description());
        kryo.writeObject(output, object.onCollect());
        kryo.writeObject(output, object.onDrop());
        kryo.writeObject(output, object.onUse());
        kryo.writeObject(output, object.damageModifier());
    }

    @Override
    public ItemData read(Kryo kryo, Input input, Class<ItemData> type) {
        ItemType itemType = kryo.readObject(input, ItemType.class);
        Animation inventoryTexture = kryo.readObject(input, Animation.class);
        Animation worldTexture = kryo.readObject(input, Animation.class);
        String itemName = input.readString();
        String description = input.readString();
        BiConsumer<Entity, Entity> onCollect = kryo.readObject(input, BiConsumer.class);
        TriConsumer<Entity, ItemData, Point> onDrop = kryo.readObject(input, TriConsumer.class);
        BiConsumer<Entity, ItemData> onUse = kryo.readObject(input, BiConsumer.class);
        DamageModifier damageModifier = kryo.readObject(input, DamageModifier.class);
        return new ItemData(itemType, inventoryTexture, worldTexture, itemName, description, onCollect, onDrop, onUse, damageModifier);
    }
}

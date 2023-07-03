package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.components.XPComponent;
import core.Entity;

import java.util.function.LongConsumer;

public class XPComponentSerializer extends Serializer<XPComponent> {
    private Entity entity;

    public XPComponentSerializer(){
        super();
    }

    public XPComponentSerializer(Entity e){
        super();
        entity = e;
    }
    @Override
    public void write(Kryo kryo, Output output, XPComponent object) {
        output.writeLong(object.currentLevel());
        output.writeLong(object.currentXP());
        output.writeLong(object.lootXP());
        kryo.writeObject(output, object.getCallbackLevelUp());
    }

    @Override
    public XPComponent read(Kryo kryo, Input input, Class<XPComponent> type) {
        long currentLevel = input.readLong();
        long currentXP = input.readLong();
        long lootXP = input.readLong();
        LongConsumer levelUp = kryo.readObject(input, LongConsumer.class);
        XPComponent xpc = new XPComponent(entity, levelUp, lootXP);
        xpc.currentXP(currentXP);
        xpc.currentLevel(currentLevel);
        return xpc;
    }
}

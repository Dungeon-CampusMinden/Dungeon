package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.components.XPComponent;
import contrib.utils.components.xp.ILevelUp;
import core.Entity;

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
        output.writeLong(object.getCurrentLevel());
        output.writeLong(object.getCurrentXP());
        output.writeLong(object.getLootXP());
        kryo.writeObject(output, object.getCallbackLevelUp());
    }

    @Override
    public XPComponent read(Kryo kryo, Input input, Class<XPComponent> type) {
        long currentLevel = input.readLong();
        long currenXP = input.readLong();
        long lootXP = input.readLong();
        ILevelUp levelUp = kryo.readObject(input, ILevelUp.class);
        return new XPComponent(entity, levelUp, currentLevel, currenXP, lootXP);
    }
}

package contrib.utils.multiplayer.network.packages.serializer.components;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.components.XPComponent;

import core.Entity;

/** Custom serializer to send and retrieve objects of {@link XPComponent}. */
public class XPComponentSerializer extends Serializer<XPComponent> {
    private Entity entity;

    /** Create new serializer for {@link XPComponent}. */
    public XPComponentSerializer() {
        super();
    }

    /**
     * Create new serializer for {@link XPComponent}.
     *
     * @param e Entity which component should be assigned to.
     */
    public XPComponentSerializer(Entity e) {
        this();
        entity = e;
    }

    @Override
    public void write(Kryo kryo, Output output, XPComponent object) {
        //        output.writeLong(object.currentXP());
        //        output.writeLong(object.lootXP());
    }

    @Override
    public XPComponent read(Kryo kryo, Input input, Class<XPComponent> type) {
        return null;
        //        long currentXP = input.readLong();
        //        long lootXP = input.readLong();
        //        XPComponent xpc = new XPComponent(entity, lootXP);
        //        xpc.currentXP(currentXP);
        //        return xpc;
    }
}

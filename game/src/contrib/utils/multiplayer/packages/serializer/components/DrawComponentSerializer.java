package contrib.utils.multiplayer.packages.serializer.components;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import core.Entity;
import core.components.DrawComponent;
import core.utils.components.draw.Animation;

import java.util.HashMap;

/** Custom serializer to send and retrieve objects of {@link DrawComponent}. */
public class DrawComponentSerializer extends Serializer<DrawComponent> {
    private Entity entity;

    /** Create new serializer for {@link DrawComponent}. */
    public DrawComponentSerializer() {
        super();
    }

    /**
     * Create new serializer for {@link DrawComponent}.
     *
     * @param e Entity which component should be assigned to.
     */
    public DrawComponentSerializer(Entity e) {
        this();
        entity = e;
    }

    @Override
    public void write(Kryo kryo, Output output, DrawComponent object) {
        kryo.writeObject(output, object.animationMap());
    }

    @Override
    public DrawComponent read(Kryo kryo, Input input, Class<DrawComponent> type) {
        final HashMap<String, Animation> animationHashMap = kryo.readObject(input, HashMap.class);
        return new DrawComponent(entity, animationHashMap);
    }
}

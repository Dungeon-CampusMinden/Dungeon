package contrib.utils.multiplayer.packages.serializer.components;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.components.CollideComponent;

import core.Entity;
import core.level.Tile;
import core.utils.Point;
import core.utils.TriConsumer;

/** Custom serializer to send and retrieve objects of {@link core.components.CameraComponent}. */
public class CollideComponentSerializer extends Serializer<CollideComponent> {
    private Entity entity;

    public CollideComponentSerializer() {
        super();
    }

    public CollideComponentSerializer(Entity e) {
        super();
        entity = e;
    }

    @Override
    public void write(Kryo kryo, Output output, CollideComponent object) {
        kryo.writeObject(output, object.offset());
        kryo.writeObject(output, object.size());
        kryo.writeObject(output, object.collideEnter());
        kryo.writeObject(output, object.collideLeave());
    }

    @Override
    public CollideComponent read(Kryo kryo, Input input, Class<CollideComponent> type) {
        Point offset = kryo.readObject(input, Point.class);
        Point size = kryo.readObject(input, Point.class);
        TriConsumer<Entity, Entity, Tile.Direction> collideEnter =
                kryo.readObject(input, TriConsumer.class);
        TriConsumer<Entity, Entity, Tile.Direction> collideLeave =
                kryo.readObject(input, TriConsumer.class);
        return new CollideComponent(entity, offset, size, collideEnter, collideLeave);
    }
}

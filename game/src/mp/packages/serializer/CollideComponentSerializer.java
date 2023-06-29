package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.components.CollideComponent;
import contrib.utils.components.collision.ICollide;
import core.Entity;
import core.utils.Point;

public class CollideComponentSerializer extends Serializer<CollideComponent> {
    private Entity entity;

    public CollideComponentSerializer(){
        super();
    }

    public CollideComponentSerializer(Entity e){
        super();
        entity = e;
    }
    @Override
    public void write(Kryo kryo, Output output, CollideComponent object) {
        kryo.writeObject(output, object.getOffset());
        kryo.writeObject(output, object.getSize());
        kryo.writeObject(output, object.getiCollideEnter());
        kryo.writeObject(output, object.getiCollideLeave());
    }

    @Override
    public CollideComponent read(Kryo kryo, Input input, Class<CollideComponent> type) {
        Point offset = kryo.readObject(input, Point.class);
        Point size = kryo.readObject(input, Point.class);
        ICollide iCollideEnter = kryo.readObject(input, ICollide.class);
        ICollide iCollideLeave = kryo.readObject(input, ICollide.class);
        return new CollideComponent(entity, offset, size, iCollideEnter, iCollideLeave);
    }
}

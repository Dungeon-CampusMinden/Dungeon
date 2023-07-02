package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.Entity;
import core.components.DrawComponent;
import core.utils.components.draw.Animation;

public class DrawComponentSerializer extends Serializer<DrawComponent> {
    private Entity entity;

    public DrawComponentSerializer(){
        super();
    }

    public DrawComponentSerializer(Entity e){
        super();
        entity = e;
    }
    @Override
    public void write(Kryo kryo, Output output, DrawComponent object) {
        kryo.writeObject(output, object.currentAnimation());
    }

    @Override
    public DrawComponent read(Kryo kryo, Input input, Class<DrawComponent> type) {
        Animation idle = kryo.readObject(input, Animation.class);
        return new DrawComponent(entity, idle);
    }
}

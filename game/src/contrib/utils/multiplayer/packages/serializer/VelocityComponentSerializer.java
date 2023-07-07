package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.Entity;
import core.components.VelocityComponent;

public class VelocityComponentSerializer extends Serializer<VelocityComponent> {
    private Entity entity;

    public VelocityComponentSerializer(){
        super();
    }

    public VelocityComponentSerializer(Entity e){
        super();
        entity = e;
    }
    @Override
    public void write(Kryo kryo, Output output, VelocityComponent object) {
        output.writeFloat(object.xVelocity());
        output.writeFloat(object.yVelocity());
        output.writeFloat(object.currentXVelocity());
        output.writeFloat(object.currentYVelocity());
    }

    @Override
    public VelocityComponent read(Kryo kryo, Input input, Class<VelocityComponent> type) {
        final float xVelocity = input.readFloat();
        final float YVelocity = input.readFloat();
        final float currentXVelocity = input.readFloat();
        final float currentYVelocity = input.readFloat();
        final VelocityComponent velocityComponent = new VelocityComponent(entity, xVelocity, YVelocity);
        velocityComponent.currentXVelocity(currentXVelocity);
        velocityComponent.currentYVelocity(currentYVelocity);
        return velocityComponent;
    }
}

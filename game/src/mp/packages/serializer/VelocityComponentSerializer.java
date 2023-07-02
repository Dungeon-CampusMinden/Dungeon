package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.Entity;
import core.components.VelocityComponent;
import core.utils.components.draw.Animation;

import java.io.Serial;

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
    }

    @Override
    public VelocityComponent read(Kryo kryo, Input input, Class<VelocityComponent> type) {
        float xVelocity = input.readFloat();
        float YVelocity = input.readFloat();
        return new VelocityComponent(entity, xVelocity, YVelocity);
    }
}

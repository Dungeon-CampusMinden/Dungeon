package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.components.HealthComponent;
import contrib.utils.components.health.IOnDeathFunction;
import core.Entity;
import core.utils.components.draw.Animation;

public class HealthComponentSerializer extends Serializer<HealthComponent> {
    private Entity entity;

    public HealthComponentSerializer(){
        super();
    }

    public HealthComponentSerializer(Entity e){
        super();
        entity = e;
    }
    @Override
    public void write(Kryo kryo, Output output, HealthComponent object) {
        output.writeInt(object.getMaximalHealthpoints());
        kryo.writeObject(output, object.getOnDeath());
        kryo.writeObject(output, object.getGetHitAnimation());
        kryo.writeObject(output, object.getDeathAnimation());
    }

    @Override
    public HealthComponent read(Kryo kryo, Input input, Class<HealthComponent> type) {
        int maximalHealthpoints = input.readInt();
        IOnDeathFunction onDeath = kryo.readObject(input, IOnDeathFunction.class);
        Animation getHitAnimation = kryo.readObject(input, Animation.class);
        Animation dieAnimation = kryo.readObject(input, Animation.class);
        return new HealthComponent(entity, maximalHealthpoints, onDeath, getHitAnimation, dieAnimation);
    }
}

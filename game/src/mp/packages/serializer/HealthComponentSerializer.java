package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.components.HealthComponent;
import core.Entity;
import core.utils.components.draw.Animation;

import java.util.function.Consumer;

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
        output.writeInt(object.maximalHealthpoints());
        output.writeInt(object.currentHealthpoints());
        kryo.writeObject(output, object.onDeath());
    }

    @Override
    public HealthComponent read(Kryo kryo, Input input, Class<HealthComponent> type) {
        int maximalHealthpoints = input.readInt();
        int currentHealthpoints = input.readInt();
        Consumer<Entity> onDeath = kryo.readObject(input, Consumer.class);
        HealthComponent hc = new HealthComponent(entity, maximalHealthpoints, onDeath);
        hc.currentHealthpoints(currentHealthpoints);
        return hc;
    }
}

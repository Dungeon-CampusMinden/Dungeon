package contrib.utils.multiplayer.packages.serializer.components;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.components.HealthComponent;

import core.Entity;

import java.util.function.Consumer;

/** Custom serializer to send and retrieve objects of {@link HealthComponent}. */
public class HealthComponentSerializer extends Serializer<HealthComponent> {
    private Entity entity;

    public HealthComponentSerializer() {
        super();
    }

    public HealthComponentSerializer(Entity e) {
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
        int maximalHealthPoints = input.readInt();
        int currentHealthPoints = input.readInt();
        Consumer<Entity> onDeath = kryo.readObject(input, Consumer.class);
        HealthComponent hc = new HealthComponent(entity, maximalHealthPoints, onDeath);
        hc.currentHealthpoints(currentHealthPoints);
        return hc;
    }
}

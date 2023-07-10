package contrib.utils.multiplayer.packages.serializer.components;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.components.StatsComponent;

import core.Entity;

/** Custom serializer to send and retrieve objects of {@link StatsComponent}. */
public class StatsComponentSerializer extends Serializer<StatsComponent> {
    private Entity entity;

    public StatsComponentSerializer() {
        super();
    }

    public StatsComponentSerializer(Entity e) {
        super();
        entity = e;
    }

    @Override
    public void write(Kryo kryo, Output output, StatsComponent object) {}

    @Override
    public StatsComponent read(Kryo kryo, Input input, Class<StatsComponent> type) {
        return new StatsComponent(entity);
    }
}

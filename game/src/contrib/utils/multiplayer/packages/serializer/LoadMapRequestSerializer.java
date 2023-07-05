package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.Entity;
import core.level.elements.ILevel;
import contrib.utils.multiplayer.packages.request.LoadMapRequest;

import java.util.HashSet;
import java.util.Set;

public class LoadMapRequestSerializer extends Serializer<LoadMapRequest> {
    @Override
    public void write(Kryo kryo, Output output, LoadMapRequest object) {
        kryo.writeObject(output, object.level());
        Set<Entity> currentEntities = object.entities();
        output.writeLong(currentEntities.size());
        currentEntities.forEach((entity) -> {
            kryo.writeObject(output, entity);
        });
        kryo.writeObject(output, object.hero());
    }

    @Override
    public LoadMapRequest read(Kryo kryo, Input input, Class<LoadMapRequest> type) {
        final ILevel level = kryo.readObject(input, ILevel.class);
        final long size = input.readLong();
        final Set<Entity> currentEntities = new HashSet<>();
        for (int i = 0; i < size; i++){
            currentEntities.add(kryo.readObject(input, Entity.class));
        }
        final Entity hero = kryo.readObject(input, Entity.class);
        return new LoadMapRequest(level, currentEntities, hero);
    }
}

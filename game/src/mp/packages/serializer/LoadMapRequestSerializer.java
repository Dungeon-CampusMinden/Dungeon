package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.Entity;
import core.level.elements.ILevel;
import core.utils.Point;
import mp.packages.request.LoadMapRequest;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoadMapRequestSerializer extends Serializer<LoadMapRequest> {
    @Override
    public void write(Kryo kryo, Output output, LoadMapRequest object) {
        kryo.writeObject(output, object.getLevel());
        Stream<Entity> currentStream = object.getCurrentEntities();
        Set<Entity> currentEntities = currentStream.collect(Collectors.toSet());
        output.writeLong(currentEntities.size());
        currentEntities.forEach((entity) -> {
            kryo.writeObject(output, entity);
        });
    }

    @Override
    public LoadMapRequest read(Kryo kryo, Input input, Class<LoadMapRequest> type) {
        final ILevel level = kryo.readObject(input, ILevel.class);
        long size = input.readLong();
        Set<Entity> currentEntities = new HashSet<>();
        for (int i = 0; i < size; i++){
            currentEntities.add(kryo.readObject(input, Entity.class));
        }
        return new LoadMapRequest(level, currentEntities.stream());
    }
}

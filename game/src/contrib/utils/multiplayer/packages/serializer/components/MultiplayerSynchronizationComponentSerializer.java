package contrib.utils.multiplayer.packages.serializer.components;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.components.MultiplayerSynchronizationComponent;
import core.Entity;
import core.utils.components.draw.Animation;

/**
 * Custom serializer to send and retrieve objects of {@link MultiplayerSynchronizationComponent}.
 */
public class MultiplayerSynchronizationComponentSerializer extends Serializer<MultiplayerSynchronizationComponent> {
    private Entity entity;

    public MultiplayerSynchronizationComponentSerializer(){
        super();
    }

    public MultiplayerSynchronizationComponentSerializer(Entity e){
        super();
        entity = e;
    }
    @Override
    public void write(Kryo kryo, Output output, MultiplayerSynchronizationComponent object) {}

    @Override
    public MultiplayerSynchronizationComponent read(Kryo kryo, Input input, Class<MultiplayerSynchronizationComponent> type) {
        return new MultiplayerSynchronizationComponent(entity);
    }
}

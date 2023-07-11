package contrib.utils.multiplayer.packages.serializer.components;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.components.MultiplayerSynchronizationComponent;

import core.Entity;

/**
 * Custom serializer to send and retrieve objects of {@link MultiplayerSynchronizationComponent}.
 */
public class MultiplayerSynchronizationComponentSerializer
        extends Serializer<MultiplayerSynchronizationComponent> {
    private Entity entity;

    /** Create new serializer for {@link MultiplayerSynchronizationComponent}. */
    public MultiplayerSynchronizationComponentSerializer() {
        super();
    }

    /**
     * Create new serializer for {@link MultiplayerSynchronizationComponent}.
     *
     * @param e Entity which component should be assigned to.
     */
    public MultiplayerSynchronizationComponentSerializer(Entity e) {
        this();
        entity = e;
    }

    @Override
    public void write(Kryo kryo, Output output, MultiplayerSynchronizationComponent object) {}

    @Override
    public MultiplayerSynchronizationComponent read(
            Kryo kryo, Input input, Class<MultiplayerSynchronizationComponent> type) {
        return new MultiplayerSynchronizationComponent(entity);
    }
}

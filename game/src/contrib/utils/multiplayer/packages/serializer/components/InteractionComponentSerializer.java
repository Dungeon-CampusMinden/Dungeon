package contrib.utils.multiplayer.packages.serializer.components;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.components.InteractionComponent;
import core.Entity;
import core.utils.components.draw.Animation;

import java.util.function.Consumer;

/**
 * Custom serializer to send and retrieve objects of {@link InteractionComponent}.
 */
public class InteractionComponentSerializer extends Serializer<InteractionComponent> {
    private Entity entity;

    public InteractionComponentSerializer(){
        super();
    }

    public InteractionComponentSerializer(Entity e){
        super();
        entity = e;
    }
    @Override
    public void write(Kryo kryo, Output output, InteractionComponent object) {
        output.writeFloat(object.radius());
        output.writeBoolean(object.getRepeatable());
        kryo.writeObject(output, object.getOnInteraction());
    }

    @Override
    public InteractionComponent read(Kryo kryo, Input input, Class<InteractionComponent> type) {
        float radius = input.readFloat();
        boolean repeatable = input.readBoolean();
        Consumer<Entity> onInteraction = kryo.readObject(input, Consumer.class);
        return new InteractionComponent(entity, radius, repeatable, onInteraction);
    }
}

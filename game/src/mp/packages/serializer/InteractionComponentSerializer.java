package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.components.InteractionComponent;
import contrib.utils.components.interaction.IInteraction;
import core.Entity;

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
        output.writeFloat(object.getRadius());
        output.writeBoolean(object.getRepeatable());
        kryo.writeObject(output, object.getOnInteraction());
    }

    @Override
    public InteractionComponent read(Kryo kryo, Input input, Class<InteractionComponent> type) {
        float radius = input.readFloat();
        boolean repeatable = input.readBoolean();
        IInteraction onInteraction = kryo.readObject(input, IInteraction.class);
        return new InteractionComponent(entity, radius, repeatable, onInteraction);
    }
}

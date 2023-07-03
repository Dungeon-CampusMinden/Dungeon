package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.components.MultiplayerComponent;
import core.Entity;

public class MultiplayerComponentSerializer extends Serializer<MultiplayerComponent> {
    private Entity entity;

    public MultiplayerComponentSerializer(){
        super();
    }

    public MultiplayerComponentSerializer(Entity e){
        super();
        entity = e;
    }
    @Override
    public void write(Kryo kryo, Output output, MultiplayerComponent object) {
        output.writeInt(object.getPlayerId());
    }

    @Override
    public MultiplayerComponent read(Kryo kryo, Input input, Class<MultiplayerComponent> type) {
        return new MultiplayerComponent(entity, input.readInt());
    }
}

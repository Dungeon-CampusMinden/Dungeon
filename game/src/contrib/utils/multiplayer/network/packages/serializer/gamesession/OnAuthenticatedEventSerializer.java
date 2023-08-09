package contrib.utils.multiplayer.network.packages.serializer.gamesession;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.utils.multiplayer.network.packages.event.OnAuthenticatedEvent;

/** Custom serializer to send and retrieve objects of {@link OnAuthenticatedEvent}. */
public class OnAuthenticatedEventSerializer extends Serializer<OnAuthenticatedEvent> {
    @Override
    public void write(Kryo kryo, Output output, OnAuthenticatedEvent object) {
        output.writeInt(object.assignedClientID());
    }

    @Override
    public OnAuthenticatedEvent read(Kryo kryo, Input input, Class<OnAuthenticatedEvent> type) {
        final int clientID = input.readInt();
        return new OnAuthenticatedEvent(clientID);
    }
}

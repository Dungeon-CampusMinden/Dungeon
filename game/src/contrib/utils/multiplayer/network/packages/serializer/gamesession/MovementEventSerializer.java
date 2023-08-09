package contrib.utils.multiplayer.network.packages.serializer.gamesession;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.utils.multiplayer.network.packages.event.MovementEvent;

import core.utils.Point;

/** Custom serializer to send and retrieve objects of {@link MovementEvent}. */
public class MovementEventSerializer extends Serializer<MovementEvent> {
    @Override
    public void write(Kryo kryo, Output output, MovementEvent object) {
        output.writeInt(object.entityGlobalID());
        kryo.writeObject(output, object.position());
        output.writeFloat(object.xVelocity());
        output.writeFloat(object.yVelocity());
    }

    @Override
    public MovementEvent read(Kryo kryo, Input input, Class<MovementEvent> type) {
        final int playerId = input.readInt();
        final Point position = kryo.readObject(input, Point.class);
        final float xVelocity = input.readFloat();
        final float yVelocity = input.readFloat();
        return new MovementEvent(playerId, position, xVelocity, yVelocity);
    }
}

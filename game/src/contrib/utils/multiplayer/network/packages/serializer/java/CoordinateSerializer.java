package contrib.utils.multiplayer.network.packages.serializer.java;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import core.level.utils.Coordinate;

/** Custom serializer to send and retrieve objects of {@link Coordinate}. */
public class CoordinateSerializer extends Serializer<Coordinate> {
    @Override
    public void write(Kryo kryo, Output output, Coordinate object) {
        output.writeInt(object.x);
        output.writeInt(object.y);
    }

    @Override
    public Coordinate read(Kryo kryo, Input input, Class<Coordinate> type) {
        return new Coordinate(input.readInt(), input.readInt());
    }
}

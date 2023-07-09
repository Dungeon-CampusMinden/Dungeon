package contrib.utils.multiplayer.packages.serializer.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.utils.Point;
import core.utils.components.draw.Animation;

/**
 * Custom serializer to send and retrieve objects of {@link Point}.
 */
public class PointSerializer extends Serializer<Point> {
    @Override
    public void write(Kryo kryo, Output output, Point object) {
        output.writeFloat(object.x);
        output.writeFloat(object.y);
    }

    @Override
    public Point read(Kryo kryo, Input input, Class<Point> type) {
        return new Point(input.readFloat(), input.readFloat());
    }
}

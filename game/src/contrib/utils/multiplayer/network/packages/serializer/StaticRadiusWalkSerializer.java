package contrib.utils.multiplayer.network.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.utils.components.ai.idle.StaticRadiusWalk;

import core.Dungeon;

/** Custom serializer to send and retrieve objects of {@link StaticRadiusWalk}. */
public class StaticRadiusWalkSerializer extends Serializer<StaticRadiusWalk> {
    @Override
    public void write(Kryo kryo, Output output, StaticRadiusWalk object) {
        kryo.writeClass(output, object.getClass());
        output.writeFloat(object.getRadius());
        output.writeInt(object.getBreakTime());
    }

    @Override
    public StaticRadiusWalk read(Kryo kryo, Input input, Class<StaticRadiusWalk> type) {
        float radius = input.readFloat();
        int breakTime = input.readInt();
        int breakTimeInSeconds = breakTime / Dungeon.frameRate();
        return new StaticRadiusWalk(radius, breakTimeInSeconds);
    }
}

package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.ai.idle.RadiusWalk;
import core.Dungeon;
import core.Game;
import core.utils.Constants;

public class RadiusWalkSerializer extends Serializer<RadiusWalk> {
    @Override
    public void write(Kryo kryo, Output output, RadiusWalk object) {
        kryo.writeClass(output, object.getClass());
        output.writeFloat(object.getRadius());
        output.writeInt(object.getBreakTime());
    }

    @Override
    public RadiusWalk read(Kryo kryo, Input input, Class<RadiusWalk> type) {
        float radius = input.readFloat();
        int breakTime = input.readInt();
        int breakTimeInSeconds = breakTime / Dungeon.frameRate();
        return new RadiusWalk(radius, breakTimeInSeconds);
    }
}

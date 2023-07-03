package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.ai.idle.PatrouilleWalk;

public class PatrouilleWalkSerializer extends Serializer<PatrouilleWalk> {
    @Override
    public void write(Kryo kryo, Output output, PatrouilleWalk object) {
        kryo.writeClass(output, object.getClass());
        output.writeFloat(object.radius());
        output.writeInt(object.numberCheckpoints());
        output.writeInt(object.pauseTime());
        kryo.writeObject(output, object.mode());
    }

    @Override
    public PatrouilleWalk read(Kryo kryo, Input input, Class<PatrouilleWalk> type) {
        float radius = input.readFloat();
        int numberCheckpoints = input.readInt();
        int pauseTime = input.readInt();
        PatrouilleWalk.MODE mode = kryo.readObject(input, PatrouilleWalk.MODE.class);
        return new PatrouilleWalk(radius, numberCheckpoints, pauseTime, mode);
    }
}

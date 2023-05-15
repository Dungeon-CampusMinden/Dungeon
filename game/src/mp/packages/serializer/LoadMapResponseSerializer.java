package mp.packages.serializer;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.level.elements.ILevel;
import core.utils.Point;
import mp.packages.response.LoadMapResponse;

import java.util.HashMap;

public class LoadMapResponseSerializer extends Serializer<LoadMapResponse> {
    @Override
    public void write(Kryo kryo, Output output, LoadMapResponse object) {
        output.writeBoolean(object.getIsSucceed());
        kryo.writeObject(output, object.getLevel());
        kryo.writeObject(output, object.getHeroPositionByClientId());
    }

    @Override
    public LoadMapResponse read(Kryo kryo, Input input, Class<LoadMapResponse> type) {
        final boolean isSucceed = input.readBoolean();
        final ILevel level = kryo.readObject(input, ILevel.class);
        final HashMap<Integer, Point> heroPositionByClientId = kryo.readObject(input, HashMap.class);
        return new LoadMapResponse(isSucceed,level, heroPositionByClientId);
    }
}

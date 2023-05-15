package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.level.elements.ILevel;
import core.utils.Point;
import mp.packages.response.JoinSessionResponse;

import java.util.HashMap;

public class JoinSessionResponseSerializer extends Serializer<JoinSessionResponse> {
    @Override
    public void write(Kryo kryo, Output output, JoinSessionResponse object) {
        output.writeBoolean(object.getIsSucceed());
        kryo.writeObject(output, object.getLevel());
        kryo.writeObject(output, object.getClientId());
        kryo.writeObject(output, object.getHeroPositionByClientId());
    }

    @Override
    public JoinSessionResponse read(Kryo kryo, Input input, Class<JoinSessionResponse> type) {
        final boolean isSucceed = input.readBoolean();
        final ILevel level = kryo.readObject(input, ILevel.class);
        final Integer clientId = kryo.readObject(input, Integer.class);
        final HashMap<Integer, Point> heroPositionByClientId = kryo.readObject(input, HashMap.class);
        return new JoinSessionResponse(isSucceed, level, clientId, heroPositionByClientId);
    }
}

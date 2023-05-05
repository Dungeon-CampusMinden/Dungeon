package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import level.elements.ILevel;
import mp.packages.response.JoinSessionResponse;
import tools.Point;

import java.util.HashMap;

public class JoinSessionResponseSerializer extends Serializer<JoinSessionResponse> {
    @Override
    public void write(Kryo kryo, Output output, JoinSessionResponse object) {
        kryo.writeObject(output, object.getLevel());
        kryo.writeObject(output, object.getClientId());
        kryo.writeObject(output, object.getHeroPositionByClientId());
    }

    @Override
    public JoinSessionResponse read(Kryo kryo, Input input, Class<JoinSessionResponse> type) {
        ILevel level = kryo.readObject(input, ILevel.class);
        Integer playerId = kryo.readObject(input, Integer.class);
        HashMap<Integer, Point> heroPositionByClientId = kryo.readObject(input, HashMap.class);
        return new JoinSessionResponse(level, playerId, heroPositionByClientId);
    }
}

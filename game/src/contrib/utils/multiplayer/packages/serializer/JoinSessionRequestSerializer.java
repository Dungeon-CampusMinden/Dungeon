package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.multiplayer.packages.request.JoinSessionRequest;
import core.Entity;

public class JoinSessionRequestSerializer extends Serializer<JoinSessionRequest> {
    @Override
    public void write(Kryo kryo, Output output, JoinSessionRequest object) {
        kryo.writeObject(output, object.hero());
    }

    @Override
    public JoinSessionRequest read(Kryo kryo, Input input, Class<JoinSessionRequest> type) {
        final Entity hero = kryo.readObject(input, Entity.class);
        return new JoinSessionRequest(hero);
    }
}

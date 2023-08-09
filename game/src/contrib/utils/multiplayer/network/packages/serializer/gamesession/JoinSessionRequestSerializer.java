package contrib.utils.multiplayer.network.packages.serializer.gamesession;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.utils.multiplayer.network.packages.request.JoinSessionRequest;

import core.Entity;

/** Custom serializer to send and retrieve objects of {@link JoinSessionRequest}. */
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

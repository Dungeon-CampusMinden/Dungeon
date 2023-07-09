package contrib.utils.multiplayer.packages.serializer.gamesession;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.multiplayer.packages.Version;
import contrib.utils.multiplayer.packages.request.JoinSessionRequest;
import core.Entity;

/**
 * Custom serializer to send and retrieve objects of {@link JoinSessionRequest}.
 */
public class JoinSessionRequestSerializer extends Serializer<JoinSessionRequest> {
    @Override
    public void write(Kryo kryo, Output output, JoinSessionRequest object) {
        kryo.writeObject(output, object.hero());
        kryo.writeObject(output, object.clientVersion());
    }

    @Override
    public JoinSessionRequest read(Kryo kryo, Input input, Class<JoinSessionRequest> type) {
        final Entity hero = kryo.readObject(input, Entity.class);
        final Version clientVersion = kryo.readObject(input, Version.class);
        return new JoinSessionRequest(hero, clientVersion);
    }
}

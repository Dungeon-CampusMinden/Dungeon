package contrib.utils.multiplayer.packages.serializer.gamesession;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.multiplayer.packages.Version;
import contrib.utils.multiplayer.packages.request.InitializeServerRequest;

/**
 * Custom serializer to send and retrieve objects of {@link InitializeServerRequest}.
 */
public class InitServerRequestSerializer extends Serializer<InitializeServerRequest> {
    @Override
    public void write(Kryo kryo, Output output, InitializeServerRequest initServerRequest) {
        kryo.writeObject(output, initServerRequest.clientVersion());
    }

    @Override
    public InitializeServerRequest read(Kryo kryo, Input input, Class<InitializeServerRequest> aClass) {
        final Version initServerRequest = kryo.readObject(input, Version.class);
        return new InitializeServerRequest(initServerRequest);
    }
}

package contrib.utils.multiplayer.network.packages.serializer.gamesession;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.utils.multiplayer.network.packages.response.AuthenticationRequest;

/** Custom serializer to send and retrieve objects of {@link AuthenticationRequest}. */
public class AuthenticationRequestSerializer extends Serializer<AuthenticationRequest> {
    @Override
    public void write(Kryo kryo, Output output, AuthenticationRequest initServerResponse) {}

    @Override
    public AuthenticationRequest read(Kryo kryo, Input input, Class<AuthenticationRequest> aClass) {
        return new AuthenticationRequest();
    }
}

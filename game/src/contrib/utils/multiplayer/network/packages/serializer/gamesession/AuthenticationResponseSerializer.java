package contrib.utils.multiplayer.network.packages.serializer.gamesession;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.utils.multiplayer.network.packages.Version;
import contrib.utils.multiplayer.network.packages.request.AuthenticationResponse;

/** Custom serializer to send and retrieve objects of {@link AuthenticationResponse}. */
public class AuthenticationResponseSerializer extends Serializer<AuthenticationResponse> {
    @Override
    public void write(Kryo kryo, Output output, AuthenticationResponse initServerRequest) {
        kryo.writeObject(output, initServerRequest.clientVersion());
    }

    @Override
    public AuthenticationResponse read(
            Kryo kryo, Input input, Class<AuthenticationResponse> aClass) {
        final Version initServerRequest = kryo.readObject(input, Version.class);
        return new AuthenticationResponse(initServerRequest);
    }
}

package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.multiplayer.packages.Version;
import contrib.utils.multiplayer.packages.request.InitServerRequest;

public class InitServerRequestSerializer extends Serializer<InitServerRequest> {
    @Override
    public void write(Kryo kryo, Output output, InitServerRequest initServerRequest) {
        kryo.writeObject(output, initServerRequest.clientVersion());
    }

    @Override
    public InitServerRequest read(Kryo kryo, Input input, Class<InitServerRequest> aClass) {
        final Version initServerRequest = kryo.readObject(input, Version.class);
        return new InitServerRequest(initServerRequest);
    }
}

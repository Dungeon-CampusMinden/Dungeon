package contrib.utils.multiplayer.packages.serializer.gamesession;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.utils.multiplayer.packages.response.InitializeServerResponse;

/** Custom serializer to send and retrieve objects of {@link InitializeServerResponse}. */
public class InitServerResponseSerializer extends Serializer<InitializeServerResponse> {
    @Override
    public void write(Kryo kryo, Output output, InitializeServerResponse initServerResponse) {
        output.writeBoolean(initServerResponse.isSucceed());
    }

    @Override
    public InitializeServerResponse read(
            Kryo kryo, Input input, Class<InitializeServerResponse> aClass) {
        return new InitializeServerResponse(input.readBoolean());
    }
}

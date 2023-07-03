package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.multiplayer.packages.response.InitServerResponse;

public class InitServerResponseSerializer extends Serializer<InitServerResponse> {
    @Override
    public void write(Kryo kryo, Output output, InitServerResponse initServerResponse) {
        output.writeBoolean(initServerResponse.isSucceed());
    }

    @Override
    public InitServerResponse read(Kryo kryo, Input input, Class<InitServerResponse> aClass) {
        return new InitServerResponse(input.readBoolean());
    }
}

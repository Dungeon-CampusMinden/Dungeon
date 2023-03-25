package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import mp.packages.response.InitializeServerResponse;

public class InitializeServerResponseSerializer extends Serializer<InitializeServerResponse> {
    @Override
    public void write(Kryo kryo, Output output, InitializeServerResponse object) {
        output.writeBoolean(object.isSucceed());
    }

    @Override
    public InitializeServerResponse read(Kryo kryo, Input input, Class<InitializeServerResponse> type) {
        boolean isSucceed = input.readBoolean();
        return new InitializeServerResponse(isSucceed);
    }
}

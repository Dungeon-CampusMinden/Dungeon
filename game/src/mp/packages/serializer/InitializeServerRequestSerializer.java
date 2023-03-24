package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import level.elements.ILevel;
import mp.packages.request.InitializeServerRequest;
import mp.packages.response.InitializeServerResponse;

public class InitializeServerRequestSerializer extends Serializer<InitializeServerRequest> {
    @Override
    public void write(Kryo kryo, Output output, InitializeServerRequest object) {
        kryo.writeObject(output, object.getLevel());
    }

    @Override
    public InitializeServerRequest read(Kryo kryo, Input input, Class<InitializeServerRequest> type) {
        ILevel level = kryo.readObject(input, ILevel.class);
        return new InitializeServerRequest(level);
    }
}

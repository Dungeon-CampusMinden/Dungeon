package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import level.elements.ILevel;
import mp.packages.request.InitializeServerRequest;
import mp.packages.response.JoinSessionResponse;

public class JoinSessionResponseSerializer extends Serializer<JoinSessionResponse> {
    @Override
    public void write(Kryo kryo, Output output, JoinSessionResponse object) {
        kryo.writeObject(output, object.getLevel());
    }

    @Override
    public JoinSessionResponse read(Kryo kryo, Input input, Class<JoinSessionResponse> type) {
        ILevel level = kryo.readObject(input, ILevel.class);
        return new JoinSessionResponse(level);
    }
}

package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import mp.packages.response.InitializeServerResponse;
import tools.Point;

public class InitializeServerResponseSerializer extends Serializer<InitializeServerResponse> {
    @Override
    public void write(Kryo kryo, Output output, InitializeServerResponse object) {
        output.writeBoolean(object.getIsSucceed());
        kryo.writeObject(output, object.getInitialHeroPosition());
    }

    @Override
    public InitializeServerResponse read(Kryo kryo, Input input, Class<InitializeServerResponse> type) {
        final boolean isSucceed = input.readBoolean();
        final Point initialHeroPosition = kryo.readObject(input, Point.class);
        return new InitializeServerResponse(isSucceed, initialHeroPosition);
    }
}

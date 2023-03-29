package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import level.elements.ILevel;
import mp.packages.response.LoadMapResponse;

public class LoadMapResponseSerializer extends Serializer<LoadMapResponse> {
    @Override
    public void write(Kryo kryo, Output output, LoadMapResponse object) {
        kryo.writeObject(output, object.getLevel());
    }

    @Override
    public LoadMapResponse read(Kryo kryo, Input input, Class<LoadMapResponse> type) {
        ILevel level = kryo.readObject(input, ILevel.class);
        return new LoadMapResponse(level);
    }
}

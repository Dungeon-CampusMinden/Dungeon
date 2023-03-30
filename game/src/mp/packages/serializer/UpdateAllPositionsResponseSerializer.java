package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import mp.packages.response.UpdateAllPositionsResponse;

import java.util.HashMap;

public class UpdateAllPositionsResponseSerializer extends Serializer<UpdateAllPositionsResponse> {

    @Override
    public void write(Kryo kryo, Output output, UpdateAllPositionsResponse object) {
        kryo.writeObject(output, object.getPlayerPositions());
    }

    @Override
    public UpdateAllPositionsResponse read(Kryo kryo, Input input, Class<UpdateAllPositionsResponse> type) {
        return new UpdateAllPositionsResponse(kryo.readObject(input, HashMap.class));
    }
}

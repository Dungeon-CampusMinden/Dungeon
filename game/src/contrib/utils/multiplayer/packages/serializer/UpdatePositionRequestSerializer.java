package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.utils.Point;
import contrib.utils.multiplayer.packages.request.UpdatePositionRequest;

public class UpdatePositionRequestSerializer extends Serializer<UpdatePositionRequest> {
    @Override
    public void write(Kryo kryo, Output output, UpdatePositionRequest object) {
        output.writeInt(object.entityGlobalID());
        kryo.writeObject(output, object.position());
    }

    @Override
    public UpdatePositionRequest read(Kryo kryo, Input input, Class<UpdatePositionRequest> type) {
        int playerId = input.readInt();
        Point position = kryo.readObject(input, Point.class);
        return new UpdatePositionRequest(playerId, position);
    }
}

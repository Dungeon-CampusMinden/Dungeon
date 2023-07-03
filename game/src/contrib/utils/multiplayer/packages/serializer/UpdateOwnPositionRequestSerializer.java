package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.utils.Point;
import contrib.utils.multiplayer.packages.request.UpdateOwnPositionRequest;

public class UpdateOwnPositionRequestSerializer extends Serializer<UpdateOwnPositionRequest> {
    @Override
    public void write(Kryo kryo, Output output, UpdateOwnPositionRequest object) {
        output.writeInt(object.getClientId());
        kryo.writeObject(output, object.getHeroPosition());
    }

    @Override
    public UpdateOwnPositionRequest read(Kryo kryo, Input input, Class<UpdateOwnPositionRequest> type) {
        int playerId = input.readInt();
        Point position = kryo.readObject(input, Point.class);
        return new UpdateOwnPositionRequest(playerId, position);
    }
}

package contrib.utils.multiplayer.packages.serializer.gamesession;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.multiplayer.packages.GameState;
import contrib.utils.multiplayer.packages.response.JoinSessionResponse;
import core.utils.Point;

/**
 * Custom serializer to send and retrieve objects of {@link JoinSessionResponse}.
 */
public class JoinSessionResponseSerializer extends Serializer<JoinSessionResponse> {
    @Override
    public void write(Kryo kryo, Output output, JoinSessionResponse object) {
        output.writeBoolean(object.isSucceed());
        output.writeInt(object.heroGlobalID());
        kryo.writeObject(output, object.gameState());
        kryo.writeObject(output, object.initialPosition());
    }

    @Override
    public JoinSessionResponse read(Kryo kryo, Input input, Class<JoinSessionResponse> type) {
        final boolean isSucceed = input.readBoolean();
        final int heroGlobalID = input.readInt();
        final GameState gameState = kryo.readObject(input, GameState.class);
        final Point initialPosition = kryo.readObject(input, Point.class);
        return new JoinSessionResponse(isSucceed, heroGlobalID, gameState, initialPosition);
    }
}

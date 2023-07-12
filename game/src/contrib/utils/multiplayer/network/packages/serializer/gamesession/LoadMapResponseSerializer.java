package contrib.utils.multiplayer.network.packages.serializer.gamesession;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.utils.multiplayer.network.packages.GameState;
import contrib.utils.multiplayer.network.packages.response.LoadMapResponse;

/** Custom serializer to send and retrieve objects of {@link LoadMapResponse}. */
public class LoadMapResponseSerializer extends Serializer<LoadMapResponse> {
    @Override
    public void write(Kryo kryo, Output output, LoadMapResponse object) {
        output.writeBoolean(object.isSucceed());
        kryo.writeObject(output, object.gameState());
    }

    @Override
    public LoadMapResponse read(Kryo kryo, Input input, Class<LoadMapResponse> type) {
        final boolean isSucceed = input.readBoolean();
        final GameState gameState = kryo.readObject(input, GameState.class);
        return new LoadMapResponse(isSucceed, gameState);
    }
}

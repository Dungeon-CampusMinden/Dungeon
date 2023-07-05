package contrib.utils.multiplayer.packages.serializer;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.multiplayer.packages.GameState;
import core.Entity;
import core.Game;
import core.level.elements.ILevel;
import core.utils.Point;
import contrib.utils.multiplayer.packages.response.LoadMapResponse;

import java.util.HashMap;

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

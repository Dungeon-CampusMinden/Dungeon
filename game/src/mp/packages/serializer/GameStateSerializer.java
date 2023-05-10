package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import mp.packages.GameState;

import java.util.HashMap;

public class GameStateSerializer extends Serializer<GameState> {
    @Override
    public void write(Kryo kryo, Output output, GameState object) {
        kryo.writeObject(output, object.getHeroPositionByClientId());
    }

    @Override
    public GameState read(Kryo kryo, Input input, Class<GameState> type) {
        return new GameState(kryo.readObject(input, HashMap.class));
    }
}

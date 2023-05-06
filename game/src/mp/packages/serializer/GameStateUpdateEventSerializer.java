package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import mp.GameState;
import mp.packages.event.GameStateUpdateEvent;

public class GameStateUpdateEventSerializer extends Serializer<GameStateUpdateEvent> {

    @Override
    public void write(Kryo kryo, Output output, GameStateUpdateEvent object) {
        kryo.writeObject(output, object.getGameState());
    }

    @Override
    public GameStateUpdateEvent read(Kryo kryo, Input input, Class<GameStateUpdateEvent> type) {
        return new GameStateUpdateEvent(kryo.readObject(input, GameState.class));
    }
}

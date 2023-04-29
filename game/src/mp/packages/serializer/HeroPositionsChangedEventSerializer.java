package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import mp.packages.event.HeroPositionsChangedEvent;

import java.util.HashMap;

public class HeroPositionsChangedEventSerializer extends Serializer<HeroPositionsChangedEvent> {

    @Override
    public void write(Kryo kryo, Output output, HeroPositionsChangedEvent object) {
        kryo.writeObject(output, object.getHeroPositionByClientId());
    }

    @Override
    public HeroPositionsChangedEvent read(Kryo kryo, Input input, Class<HeroPositionsChangedEvent> type) {
        return new HeroPositionsChangedEvent(kryo.readObject(input, HashMap.class));
    }
}

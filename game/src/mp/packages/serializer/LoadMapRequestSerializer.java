package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.level.elements.ILevel;
import core.utils.Point;
import mp.packages.request.LoadMapRequest;

public class LoadMapRequestSerializer extends Serializer<LoadMapRequest> {
    @Override
    public void write(Kryo kryo, Output output, LoadMapRequest object) {
        kryo.writeObject(output, object.getLevel());
        final Point initialHeroPosition = object.getHeroInitialPosition();
        if (initialHeroPosition != null) {
            // Write boolean needed to indicate whether initialHeroPosition is null or not on later read
            output.writeBoolean(true);
            kryo.writeObject(output, initialHeroPosition);
        } else {
            // Write boolean needed to indicate whether initialHeroPosition is null or not on later read
            output.writeBoolean(false);
        }
    }

    @Override
    public LoadMapRequest read(Kryo kryo, Input input, Class<LoadMapRequest> type) {
        final ILevel level = kryo.readObject(input, ILevel.class);

        final boolean hasInitialHeroPosition = input.readBoolean();
        if (hasInitialHeroPosition) {
            final Point initialHeroPosition = kryo.readObject(input, Point.class);
            return new LoadMapRequest(level, initialHeroPosition);
        }

        return new LoadMapRequest(level);
    }
}

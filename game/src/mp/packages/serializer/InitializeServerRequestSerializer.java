package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import level.elements.ILevel;
import mp.packages.request.InitializeServerRequest;
import tools.Point;

public class InitializeServerRequestSerializer extends Serializer<InitializeServerRequest> {
    @Override
    public void write(Kryo kryo, Output output, InitializeServerRequest object) {
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
    public InitializeServerRequest read(Kryo kryo, Input input, Class<InitializeServerRequest> type) {
        final ILevel level = kryo.readObject(input, ILevel.class);

        final boolean hasInitialHeroPosition = input.readBoolean();
        if (hasInitialHeroPosition) {
            final Point initialHeroPosition = kryo.readObject(input, Point.class);
            return new InitializeServerRequest(level, initialHeroPosition);
        }

        return new InitializeServerRequest(level);
    }
}

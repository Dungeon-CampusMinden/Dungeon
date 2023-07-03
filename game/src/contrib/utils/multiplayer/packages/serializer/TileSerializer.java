package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;

import java.lang.reflect.Constructor;

public class TileSerializer extends Serializer<Tile> {
    @Override
    public void write(Kryo kryo, Output output, Tile object) {
        Class<?> concreteClass = object.getClass();
        kryo.writeClass(output, concreteClass);
        output.writeString(object.texturePath());
        kryo.writeObject(output, object.coordinate());
        output.writeInt(object.designLabel().ordinal());
    }

    @Override
    public Tile read(Kryo kryo, Input input, Class<Tile> type) {
        Class<? extends Tile> concreteClass = kryo.readClass(input).getType();
        String texturePath = input.readString();
        Coordinate globalPosition = kryo.readObject(input, Coordinate.class);
        DesignLabel designLabel = DesignLabel.values()[input.readInt()];
        try {
            Constructor<? extends Tile> constructor = concreteClass.getConstructor(
                String.class,
                Coordinate.class,
                DesignLabel.class,
                ILevel.class
            );
            Tile instance = constructor.newInstance(texturePath, globalPosition, designLabel, null);
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

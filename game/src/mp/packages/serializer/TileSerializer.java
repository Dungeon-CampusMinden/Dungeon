package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import level.elements.ILevel;
import level.elements.tile.Tile;
import level.tools.Coordinate;
import level.tools.DesignLabel;

import java.lang.reflect.Constructor;

public class TileSerializer extends Serializer<Tile> {
    @Override
    public void write(Kryo kryo, Output output, Tile object) {
        Class<?> concreteClass = object.getClass();
        kryo.writeClass(output, concreteClass);
        output.writeString(object.getTexturePath());
        kryo.writeObject(output, object.getCoordinate());
        output.writeInt(object.getDesignLabel().ordinal());
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

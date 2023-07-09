package contrib.utils.multiplayer.packages.serializer.level;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.elements.tile.FloorTile;
import core.utils.components.draw.Animation;

import java.lang.reflect.Constructor;

/**
 * Custom serializer to send and retrieve objects of {@link ILevel}.
 */
public class ILevelSerializer extends Serializer<ILevel> {
    @Override
    public void write(Kryo kryo, Output output, ILevel object) {
        Class<? extends ILevel> concreteClass = object.getClass();
        kryo.writeClass(output, concreteClass);
        kryo.writeObject(output, object.layout());
        kryo.writeObject(output, object.startTile());
    }

    @Override
    public ILevel read(Kryo kryo, Input input, Class<ILevel> type) {
        Class<? extends ILevel> concreteClass = kryo.readClass(input).getType();
        Tile[][] layout = kryo.readObject(input, Tile[][].class);
        try {
            Constructor<? extends ILevel> constructor = concreteClass.getConstructor(Tile[][].class);
            ILevel instance = constructor.newInstance(new Object[]{layout});
            instance.startTile(kryo.readObject(input, FloorTile.class));
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
